import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CharacterService } from '../../services/character.service';
import { ChatService } from '../../services/chat.service';
import { MessageService } from '../../services/message.service';
import { UserService } from '../../services/user.service';
import { Character } from '../../models/character';
import { Message } from '../../models/message';
import { MessageRoleEnum } from '../../enums/messageRoleEnum';
import { Chat } from '../../models/chat';
import { ChatSidebar } from '../chat-sidebar/chat-sidebar';
import { ChatPane } from '../chat-pane/chat-pane';

@Component({
  selector: 'app-chat-layout',
  imports: [ChatSidebar, ChatPane],
  templateUrl: './chat-layout.html',
  styleUrl: './chat-layout.scss'
})
export class ChatLayout implements OnInit {
  messages: Message[] = [];
  private messagesByCharacter: Record<string, Message[]> = {};
  
  draft = '';
  isTyping = false;
  characters: Character[] = [];
  selectedCharacterId: string | null = null;
  chat!: Chat;

  constructor(
    private router: Router,
    private userService: UserService,
    private messageService: MessageService,
    private characterService: CharacterService,
    private chatService: ChatService
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

  startNewChat() {
    if (!this.selectedCharacterId) {
      return;
    }

    const greeting = this.buildGreeting();
    this.messagesByCharacter[this.selectedCharacterId] = greeting ? [greeting] : [];
    this.messages = this.messagesByCharacter[this.selectedCharacterId];

    const userId = (typeof window !== 'undefined') ? localStorage.getItem('userId') : null;
    const payload: Partial<Chat> = {
      ownerId: userId || undefined,
      characterId: this.selectedCharacterId,
      messageList: []
    };

    this.chatService.createChat(payload).subscribe({
      next: (created) => {
        this.chat = created;
        console.debug('Chat created', created);
      },
      error: (err) => {
        console.error('Failed to create chat', err);
      }
    });
  }

  selectCharacter(id: string | undefined | null) {
    if (!id) return;
    this.selectedCharacterId = id;
    if (!this.messagesByCharacter[id]) {
      const greeting = this.buildGreeting();
      this.messagesByCharacter[id] = greeting ? [greeting] : [];
    }
    this.messages = this.messagesByCharacter[id];
  }

  clearChat() {
    if (!this.selectedCharacterId) {
      this.messages = [];
      return;
    }
    const greeting = this.buildGreeting();
    this.messagesByCharacter[this.selectedCharacterId] = greeting ? [greeting] : [];
    this.messages = this.messagesByCharacter[this.selectedCharacterId];
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
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
        this.router.navigate(['/login']);
        console.log('User logged out successfully');
      }
    });
  }

  onSendMessage(content: string) {
    if (!content.trim()) return;
    if (!this.selectedCharacterId) {
      alert('Please select a character to chat with.');
      return;
    }

    this.messages.push({ role: MessageRoleEnum.USER, content: content, timestamp: new Date() });
    this.draft = '';

    this.isTyping = true;
    const history: Message[] = this.messages;

    this.messageService.sendChatMessage(this.selectedCharacterId, history, content).subscribe({
      next: (resp) => {
        this.isTyping = false;
        const replyText = resp.content;
        const ts = resp.timestamp ? new Date(resp.timestamp as any) : new Date();
        this.messages.push({ role: MessageRoleEnum.ASSISTANT, content: replyText, timestamp: ts });
      },
      error: (err) => {
        this.isTyping = false;
        console.error('Chat failed', err);
        this.messages.push({ role: MessageRoleEnum.ASSISTANT, content: 'Sorry, I had trouble responding. Please try again.', timestamp: new Date() });
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
