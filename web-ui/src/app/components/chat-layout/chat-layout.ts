import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MessageRoleEnum } from '../../enums/messageRoleEnum';
import { Character } from '../../models/character';
import { Chat } from '../../models/chat';
import { Message } from '../../models/message';
import { CharacterService } from '../../services/character.service';
import { ChatService } from '../../services/chat.service';
import { JwtService } from '../../services/jwt.service';
import { MessageService } from '../../services/message.service';
import { UserService } from '../../services/user.service';
import { CharacterInfo } from '../character-info/character-info';
import { ChatPane } from '../chat-pane/chat-pane';
import { ChatSidebar } from '../chat-sidebar/chat-sidebar';
import { NavbarComponent } from '../navbar/navbar';

/**
 * Main chat layout component that manages the overall chat experience.
 * Coordinates character selection, chat sessions, and message flow.
 */
@Component({
  selector: 'app-chat-layout',
  imports: [ChatSidebar, ChatPane, NavbarComponent, CharacterInfo],
  templateUrl: './chat-layout.html',
  styleUrl: './chat-layout.scss'
})
export class ChatLayout implements OnInit, OnDestroy {
  // UI State
  messages: Message[] = [];
  draft = '';
  isTyping = false;
  characters: Character[] = [];
  selectedCharacterId: string | null = null;
  currentChat: Chat | null = null;
  pastChatsByCharacter: Record<string, Chat[]> = {};

  // Internal State Management
  private messagesByCharacter: Record<string, Message[]> = {};
  private chatByCharacter: Record<string, Chat> = {};
  private destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly userService: UserService,
    private readonly messageService: MessageService,
    private readonly characterService: CharacterService,
    private readonly chatService: ChatService,
    private readonly jwtService: JwtService
  ) { }

  ngOnInit(): void {
    this.loadCharacters();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ============================================================================
  // Getters
  // ============================================================================

  /**
   * Returns the currently active character based on selectedCharacterId
   */
  get activeCharacter(): Character | undefined {
    return this.characters.find(c => c.id === this.selectedCharacterId);
  }

  /**
   * Returns the current logged-in username
   */
  get currentUsername(): string | null {
    return this.jwtService.getUsername();
  }

  // ============================================================================
  // Character Management
  // ============================================================================

  /**
   * Loads all available characters from the server
   */
  private loadCharacters(): void {
    this.characterService.getCharacters()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (chars) => {
          this.characters = chars;
          this.selectFirstCharacterIfNoneSelected(chars);
        },
        error: (err) => console.error('Failed to load characters', err)
      });
  }

  /**
   * Automatically selects the first character if none is currently selected
   */
  private selectFirstCharacterIfNoneSelected(chars: Character[]): void {
    if (chars.length && !this.selectedCharacterId) {
      const firstId = chars[0].id;
      if (firstId) {
        this.selectCharacter(firstId);
      }
    }
  }

  /**
   * Navigates to character creation page
   */
  createNewCharacter(): void {
    this.router.navigate(['/character']);
  }

  // ============================================================================
  // Chat Session Management
  // ============================================================================

  /**
   * Selects a character and loads/creates their chat session
   */
  selectCharacter(id: string | undefined | null): void {
    if (!id) return;

    this.selectedCharacterId = id;

    // Check if we already have a chat loaded for this character
    if (this.chatByCharacter[id]) {
      this.loadCachedChat(id);
      return;
    }

    // Try to fetch existing chat from server
    this.loadOrCreateChatForCharacter(id);
  }

  /**
   * Loads a previously cached chat from memory
   */
  private loadCachedChat(characterId: string): void {
    this.currentChat = this.chatByCharacter[characterId];
    this.messages = this.messagesByCharacter[characterId] || [];
  }

  /**
   * Attempts to load an existing chat or creates a new one if none exists
   */
  private loadOrCreateChatForCharacter(characterId: string): void {
    const userId = this.jwtService.getUserId();

    if (!userId) {
      this.startNewChat();
      return;
    }

    this.chatService.getLatestChat(characterId, userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (chat) => this.handleExistingChatLoaded(characterId, chat),
        error: () => {
          console.debug('No existing chat found, starting new chat');
          this.startNewChat();
        }
      });
  }

  /**
   * Handles successful loading of an existing chat
   */
  private handleExistingChatLoaded(characterId: string, chat: Chat): void {
    this.currentChat = chat;
    this.chatByCharacter[characterId] = chat;

    const loadedMessages = this.initializeMessagesWithGreeting(chat.messageList || []);
    this.messagesByCharacter[characterId] = loadedMessages;
    this.messages = loadedMessages;

    console.debug('Loaded existing chat', chat);
  }

  /**
   * Ensures messages array has at least a greeting message
   */
  private initializeMessagesWithGreeting(messages: Message[]): Message[] {
    if (messages.length === 0) {
      const greeting = this.buildGreeting();
      if (greeting) {
        messages.push(greeting);
      }
    }
    return messages;
  }

  /**
   * Starts a new chat session with the selected character
   */
  startNewChat(): void {
    if (!this.selectedCharacterId) return;

    const greeting = this.buildGreeting();
    const initialMessages = greeting ? [greeting] : [];

    this.messagesByCharacter[this.selectedCharacterId] = initialMessages;
    this.messages = initialMessages;

    this.createNewChatOnServer(this.selectedCharacterId, initialMessages);
  }

  /**
   * Creates a new chat record on the server
   */
  private createNewChatOnServer(characterId: string, messages: Message[]): void {
    const userId = this.jwtService.getUserId();
    const payload: Partial<Chat> = {
      ownerId: userId || undefined,
      characterId: characterId,
      messageList: messages
    };

    this.chatService.createChat(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (created) => {
          this.currentChat = created;
          this.chatByCharacter[characterId] = created;
          console.debug('New chat created', created);
        },
        error: (err) => console.error('Failed to create chat', err)
      });
  }

  /**
   * Clears the current chat and starts a new one
   */
  clearChat(): void {
    if (!this.selectedCharacterId) {
      this.messages = [];
      return;
    }

    this.messagesByCharacter[this.selectedCharacterId] = [];
    this.messages = [];
    delete this.chatByCharacter[this.selectedCharacterId];
    this.startNewChat();
  }

  // ============================================================================
  // Past Chats Management
  // ============================================================================

  /**
   * Loads past chat history for a specific character
   */
  onLoadPastChats(characterId: string): void {
    const userId = this.jwtService.getUserId();
    if (!userId) return;

    this.chatService.getChatsByCharacter(characterId, userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (chats) => {
          this.pastChatsByCharacter[characterId] = this.filterAndSortPastChats(chats);
        },
        error: (err) => {
          console.error('Failed to load past chats', err);
          this.pastChatsByCharacter[characterId] = [];
        }
      });
  }

  /**
   * Filters out current chat and sorts remaining chats by date
   */
  private filterAndSortPastChats(chats: Chat[]): Chat[] {
    return chats
      .filter(chat => chat.id !== this.currentChat?.id)
      .sort((a, b) => {
        const dateA = new Date(a.updatedAt || a.createdAt || 0).getTime();
        const dateB = new Date(b.updatedAt || b.createdAt || 0).getTime();
        return dateB - dateA;
      });
  }

  /**
   * Loads a previously saved chat when selected from history
   */
  onSelectPastChat(chat: Chat): void {
    if (!chat.id || !chat.characterId) return;

    this.selectedCharacterId = chat.characterId;
    this.currentChat = chat;
    this.chatByCharacter[chat.characterId] = chat;

    const loadedMessages = this.initializeMessagesWithGreeting(chat.messageList || []);
    this.messagesByCharacter[chat.characterId] = loadedMessages;
    this.messages = loadedMessages;
  }

  // ============================================================================
  // Message Handling
  // ============================================================================

  /**
   * Sends a user message and gets AI response
   */
  onSendMessage(content: string): void {
    if (!this.validateSendMessage(content)) return;

    const userMessage = this.createUserMessage(content);
    this.messages.push(userMessage);
    this.draft = '';

    this.sendMessageToAI(content);
  }

  /**
   * Validates that a message can be sent
   */
  private validateSendMessage(content: string): boolean {
    if (!content.trim()) return false;

    if (!this.selectedCharacterId) {
      alert('Please select a character to chat with.');
      return false;
    }

    if (!this.currentChat?.id) {
      alert('Please wait for the chat to be initialized.');
      return false;
    }

    return true;
  }

  /**
   * Creates a user message object
   */
  private createUserMessage(content: string): Message {
    return {
      role: MessageRoleEnum.USER,
      content: content,
      timestamp: new Date()
    };
  }

  /**
   * Sends message to AI service and handles response
   */
  private sendMessageToAI(content: string): void {
    if (!this.currentChat?.id || !this.selectedCharacterId) return;

    this.isTyping = true;
    const history = this.messages.slice(0, -1);

    this.messageService.sendChatMessage(
      this.currentChat.id,
      this.selectedCharacterId,
      history,
      content
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp) => this.handleAIResponse(resp),
        error: (err) => this.handleAIError(err)
      });
  }

  /**
   * Handles successful AI response
   */
  private handleAIResponse(response: Message): void {
    this.isTyping = false;

    const aiMessage: Message = {
      role: MessageRoleEnum.ASSISTANT,
      content: response.content,
      timestamp: response.timestamp ? new Date(response.timestamp as any) : new Date()
    };

    this.messages.push(aiMessage);
    this.updateCachedMessages();
  }

  /**
   * Handles AI response error
   */
  private handleAIError(err: any): void {
    this.isTyping = false;
    console.error('Chat failed', err);

    const errorMessage: Message = {
      role: MessageRoleEnum.ASSISTANT,
      content: 'Sorry, I had trouble responding. Please try again.',
      timestamp: new Date()
    };

    this.messages.push(errorMessage);
  }

  /**
   * Updates the cached messages for the current character
   */
  private updateCachedMessages(): void {
    if (this.selectedCharacterId) {
      this.messagesByCharacter[this.selectedCharacterId] = this.messages;
    }
  }

  /**
   * Handles editing of an existing message
   */
  onEditMessage(updatedMessage: Message): void {
    this.messageService.updateMessage(updatedMessage)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (savedMessage) => this.handleMessageUpdateSuccess(savedMessage),
        error: (err) => this.handleMessageUpdateError(err)
      });
  }

  /**
   * Handles successful message update
   */
  private handleMessageUpdateSuccess(savedMessage: Message): void {
    const index = this.messages.findIndex(m => m.id === savedMessage.id);

    if (index !== -1) {
      this.messages[index] = savedMessage;
      this.updateCachedMessages();
      console.debug('Message updated successfully', savedMessage);
    }
  }

  /**
   * Handles message update error
   */
  private handleMessageUpdateError(err: any): void {
    console.error('Failed to update message', err);
    alert('Failed to update message. Please try again.');
  }

  // ============================================================================
  // User Actions
  // ============================================================================

  /**
   * Handles user logout
   */
  onLogout(): void {
    this.userService.logout()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => console.log('Logout successful', response),
        error: (error) => console.error('Logout failed', error),
        complete: () => this.completeLogout()
      });
  }

  /**
   * Completes the logout process
   */
  private completeLogout(): void {
    this.jwtService.clearToken();
    this.router.navigate(['/login']);
    console.log('User logged out successfully');
  }

  /**
   * Toggles settings panel (placeholder)
   */
  toggleSettings(): void {
    alert('Settings panel coming soon.');
  }

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Builds a greeting message from the active character
   */
  private buildGreeting(): Message | null {
    const character = this.activeCharacter;
    if (!character) return null;

    const greetingText = character.shortGreeting || 'Hi! How can I help you today? 😊';

    return {
      role: MessageRoleEnum.ASSISTANT,
      content: greetingText,
      timestamp: new Date()
    };
  }
}