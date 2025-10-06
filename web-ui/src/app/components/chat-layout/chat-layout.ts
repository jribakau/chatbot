import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageRoleEnum } from '../../enums/messageRoleEnum';
import { Character } from '../../models/character';
import { Chat } from '../../models/chat';
import { Message } from '../../models/message';
import { CharacterService } from '../../services/character.service';
import { ChatService } from '../../services/chat.service';
import { JwtService } from '../../services/jwt.service';
import { MessageService } from '../../services/message.service';
import { UserService } from '../../services/user.service';
import { ChatPane } from '../chat-pane/chat-pane';
import { ChatSidebar } from '../chat-sidebar/chat-sidebar';
import { NavbarComponent } from '../navbar/navbar';

@Component({
  selector: 'app-chat-layout',
  imports: [ChatSidebar, ChatPane, NavbarComponent],
  templateUrl: './chat-layout.html',
  styleUrl: './chat-layout.scss'
})
export class ChatLayout implements OnInit {
  messages: Message[] = [];
  private messagesByCharacter: Record<string, Message[]> = {};
  private chatByCharacter: Record<string, Chat> = {};
  pastChatsByCharacter: Record<string, Chat[]> = {};

  draft = '';
  isTyping = false;
  characters: Character[] = [];
  selectedCharacterId: string | null = null;
  currentChat: Chat | null = null;

  constructor(
    private router: Router,
    private userService: UserService,
    private messageService: MessageService,
    private characterService: CharacterService,
    private chatService: ChatService,
    private jwtService: JwtService
  ) { }

  ngOnInit(): void {
    this.loadCharacters();
  }

  private loadCharacters() {
    this.characterService.getCharacters().subscribe({
      next: (chars) => {
        this.characters = chars;
        if (chars.length && !this.selectedCharacterId) {
          const firstId = chars[0].id || null;
          if (firstId) {
            this.selectCharacter(firstId);
          }
        }
      },
      error: (err) => console.error('Failed to load characters', err)
    });
  }

  get activeCharacter(): Character | undefined {
    return this.characters.find(c => c.id === this.selectedCharacterId);
  }

  get currentUsername(): string | null {
    return this.jwtService.getUsername();
  }

  createNewCharacter() {
    this.router.navigate(['/character']);
  }

  startNewChat() {
    if (!this.selectedCharacterId) {
      return;
    }

    const greeting = this.buildGreeting();
    this.messagesByCharacter[this.selectedCharacterId] = greeting ? [greeting] : [];
    this.messages = this.messagesByCharacter[this.selectedCharacterId];

    const userId = this.jwtService.getUserId();
    const payload: Partial<Chat> = {
      ownerId: userId || undefined,
      characterId: this.selectedCharacterId,
      messageList: greeting ? [greeting] : []
    };

    this.chatService.createChat(payload).subscribe({
      next: (created) => {
        this.currentChat = created;
        this.chatByCharacter[this.selectedCharacterId!] = created;
        console.debug('New chat created', created);
      },
      error: (err) => {
        console.error('Failed to create chat', err);
      }
    });
  }

  selectCharacter(id: string | undefined | null) {
    if (!id) return;
    this.selectedCharacterId = id;

    if (this.chatByCharacter[id]) {
      this.currentChat = this.chatByCharacter[id];
      this.messages = this.messagesByCharacter[id] || [];
      return;
    }

    const userId = this.jwtService.getUserId();
    if (userId) {
      this.chatService.getLatestChat(id, userId).subscribe({
        next: (chat) => {
          this.currentChat = chat;
          this.chatByCharacter[id] = chat;

          const loadedMessages = chat.messageList || [];

          if (loadedMessages.length === 0) {
            const greeting = this.buildGreeting();
            if (greeting) {
              loadedMessages.push(greeting);
            }
          }

          this.messagesByCharacter[id] = loadedMessages;
          this.messages = loadedMessages;

          console.debug('Loaded existing chat', chat);
        },
        error: (err) => {
          console.debug('No existing chat found, starting new chat');
          this.startNewChat();
        }
      });
    } else {
      this.startNewChat();
    }
  }

  clearChat() {
    if (!this.selectedCharacterId) {
      this.messages = [];
      return;
    }

    this.messagesByCharacter[this.selectedCharacterId] = [];
    this.messages = [];
    delete this.chatByCharacter[this.selectedCharacterId];
    this.startNewChat();
  }

  toggleSettings() {
    alert('Settings panel coming soon.');
  }

  onLogout() {
    this.userService.logout().subscribe({
      next: (response) => {
        console.log('Logout successful', response);
      },
      error: (error) => {
        console.error('Logout failed', error);
      },
      complete: () => {
        this.jwtService.clearToken();
        this.router.navigate(['/login']);
        console.log('User logged out successfully');
      }
    });
  }

  onLoadPastChats(characterId: string) {
    const userId = this.jwtService.getUserId();
    if (!userId) return;

    this.chatService.getChatsByCharacter(characterId, userId).subscribe({
      next: (chats) => {
        const pastChats = chats
          .filter(chat => chat.id !== this.currentChat?.id)
          .sort((a, b) => {
            const dateA = new Date(a.updatedAt || a.createdAt || 0).getTime();
            const dateB = new Date(b.updatedAt || b.createdAt || 0).getTime();
            return dateB - dateA;
          });
        this.pastChatsByCharacter[characterId] = pastChats;
      },
      error: (err) => {
        console.error('Failed to load past chats', err);
        this.pastChatsByCharacter[characterId] = [];
      }
    });
  }

  onSelectPastChat(chat: Chat) {
    if (!chat.id || !chat.characterId) return;

    this.selectedCharacterId = chat.characterId;
    this.currentChat = chat;
    this.chatByCharacter[chat.characterId] = chat;

    const loadedMessages = chat.messageList || [];
    if (loadedMessages.length === 0) {
      const greeting = this.buildGreeting();
      if (greeting) {
        loadedMessages.push(greeting);
      }
    }

    this.messagesByCharacter[chat.characterId] = loadedMessages;
    this.messages = loadedMessages;

  }

  onSendMessage(content: string) {
    if (!content.trim()) return;
    if (!this.selectedCharacterId) {
      alert('Please select a character to chat with.');
      return;
    }

    if (!this.currentChat || !this.currentChat.id) {
      alert('Please wait for the chat to be initialized.');
      return;
    }

    const userMsg: Message = { role: MessageRoleEnum.USER, content: content, timestamp: new Date() };
    this.messages.push(userMsg);
    this.draft = '';

    this.isTyping = true;
    const history: Message[] = this.messages.slice(0, -1);

    this.messageService.sendChatMessage(this.currentChat.id, this.selectedCharacterId, history, content).subscribe({
      next: (resp) => {
        this.isTyping = false;
        const replyText = resp.content;
        const ts = resp.timestamp ? new Date(resp.timestamp as any) : new Date();
        const aiMsg: Message = { role: MessageRoleEnum.ASSISTANT, content: replyText, timestamp: ts };
        this.messages.push(aiMsg);

        if (this.selectedCharacterId) {
          this.messagesByCharacter[this.selectedCharacterId] = this.messages;
        }
      },
      error: (err) => {
        this.isTyping = false;
        console.error('Chat failed', err);
        this.messages.push({ role: MessageRoleEnum.ASSISTANT, content: 'Sorry, I had trouble responding. Please try again.', timestamp: new Date() });
      }
    });
  }

  onEditMessage(updatedMessage: Message) {
    this.messageService.updateMessage(updatedMessage).subscribe({
      next: (savedMessage) => {
        const index = this.messages.findIndex(m => m.id === savedMessage.id);
        if (index !== -1) {
          this.messages[index] = savedMessage;

          if (this.selectedCharacterId) {
            this.messagesByCharacter[this.selectedCharacterId] = this.messages;
          }
        }
        console.debug('Message updated successfully', savedMessage);
      },
      error: (err) => {
        console.error('Failed to update message', err);
        alert('Failed to update message. Please try again.');
      }
    });
  }

  private buildGreeting(): Message | null {
    const ch = this.activeCharacter;
    if (!ch) return null;
    const text = ch.shortGreeting || 'Hi! How can I help you today? 😊';
    return { role: MessageRoleEnum.ASSISTANT, content: text, timestamp: new Date() };
  }
}
