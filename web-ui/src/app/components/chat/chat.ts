import { Component, ElementRef, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ChatService } from '../../services/chat.service';
import { Character } from '../../models/character';
import { ChatMessage } from '../../models/chatMessage';
import { MessageRoleEnum } from '../../enums/messageRoleEnum';
import { CharacterService } from '../../services/character.service';

interface ConversationMeta {
  title: string;
  icon: string;
  lastMessage: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss']
})
export class ChatComponent implements OnInit {
  @ViewChild('messagesScroll') messagesScroll?: ElementRef<HTMLDivElement>;

  conversations: ConversationMeta[] = [
    { title: 'New chat', icon: '💬', lastMessage: 'Start a new conversation' }
  ];
  activeConversationIndex = 0;

  messages: ChatMessage[] = [
    { role: MessageRoleEnum.ASSISTANT, content: 'Hi! How can I help you today? 😊', timestamp: new Date() },
  ];

  draft = '';
  isTyping = false;
  characters: Character[] = [];
  selectedCharacterId: string | null = null;

  constructor(
    private router: Router,
    private userService: UserService,
    private chatService: ChatService,
    private characterService: CharacterService
  ) {}

  ngOnInit(): void {
    this.loadCharacters();
  }

  private loadCharacters() {
    this.characterService.getCharacters().subscribe({
      next: (chars) => {
        this.characters = chars;
        if (chars.length && !this.selectedCharacterId) {
          this.selectedCharacterId = chars[0].id || null;
          this.updateActiveConversationHeader();
        }
      },
      error: (err) => console.error('Failed to load characters', err)
    });
  }

  get activeConversation(): ConversationMeta | undefined {
    return this.conversations[this.activeConversationIndex];
  }

  startNewChat() {
    this.conversations.unshift({ title: 'New chat', icon: '💬', lastMessage: 'Start a new conversation' });
    this.activeConversationIndex = 0;
    this.messages = [{ role: MessageRoleEnum.ASSISTANT, content: 'New chat started! What’s on your mind?', timestamp: new Date() }];
    this.scrollToBottomSoon();
  }

  switchConversation(index: number) {
    this.activeConversationIndex = index;
    // For demo purposes, conversations do not persist distinct messages.
    this.scrollToBottomSoon();
  }

  clearChat() {
    this.messages = [];
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

  onKeyDown(ev: KeyboardEvent) {
    if (ev.key === 'Enter' && !ev.shiftKey) {
      ev.preventDefault();
      this.send();
    }
  }

  send() {
    const content = this.draft.trim();
    if (!content) return;
    if (!this.selectedCharacterId) {
      alert('Please select a character to chat with.');
      return;
    }

    this.messages.push({ role: MessageRoleEnum.USER, content: content, timestamp: new Date() });
    this.draft = '';
    this.scrollToBottomSoon();

    // Call backend chat
    this.isTyping = true;
    const history: ChatMessage[] = this.messages;

    this.chatService.sendChatMessage(this.selectedCharacterId, history, content).subscribe({
      next: (resp) => {
        this.isTyping = false;
        const replyText = resp.content;
        const ts = resp.timestamp ? new Date(resp.timestamp as any) : new Date();
        this.messages.push({ role: MessageRoleEnum.ASSISTANT, content: replyText, timestamp: ts });
        this.conversations[this.activeConversationIndex].lastMessage = replyText;
        this.updateConversationTitleIfNeeded(content);
        this.scrollToBottomSoon();
      },
      error: (err) => {
        this.isTyping = false;
        console.error('Chat failed', err);
        this.messages.push({ role: MessageRoleEnum.ASSISTANT, content: 'Sorry, I had trouble responding. Please try again.', timestamp: new Date() });
        this.scrollToBottomSoon();
      }
    });
  }

  private scrollToBottomSoon() {
    setTimeout(() => {
      const el = this.messagesScroll?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }

  private updateConversationTitleIfNeeded(firstUserMessage: string) {
    if (this.conversations[this.activeConversationIndex].title === 'New chat') {
      this.conversations[this.activeConversationIndex].title = firstUserMessage.slice(0, 24) || 'Conversation';
    }
  }

  onCharacterChange() {
    this.updateActiveConversationHeader();
  }

  private updateActiveConversationHeader() {
    const current = this.conversations[this.activeConversationIndex];
    const char = this.characters.find(c => c.id === this.selectedCharacterId);
    if (char) {
      current.icon = (char.name?.[0]?.toUpperCase() || '🤖');
      current.title = char.name || 'Character';
    }
  }
}
