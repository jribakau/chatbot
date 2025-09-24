import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';

type Role = 'user' | 'assistant';

interface ChatMessage {
  role: Role;
  text: string;
  timestamp: Date;
}

interface ConversationMeta {
  title: string;
  icon: string; // emoji or initial
  lastMessage: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss']
})
export class ChatComponent {
  @ViewChild('messagesScroll') messagesScroll?: ElementRef<HTMLDivElement>;

  conversations: ConversationMeta[] = [
    { title: 'New chat', icon: '💬', lastMessage: 'Start a new conversation' }
  ];
  activeConversationIndex = 0;

  messages: ChatMessage[] = [
    { role: 'assistant', text: 'Hi! How can I help you today? 😊', timestamp: new Date() },
  ];

  draft = '';
  isTyping = false;

  constructor(
    private router: Router,
    private userService: UserService
  ) {}

  get activeConversation(): ConversationMeta | undefined {
    return this.conversations[this.activeConversationIndex];
  }

  startNewChat() {
    this.conversations.unshift({ title: 'New chat', icon: '💬', lastMessage: 'Start a new conversation' });
    this.activeConversationIndex = 0;
    this.messages = [{ role: 'assistant', text: 'New chat started! What’s on your mind?', timestamp: new Date() }];
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

    this.messages.push({ role: 'user', text: content, timestamp: new Date() });
    this.draft = '';
    this.scrollToBottomSoon();

    // Simulate assistant typing & reply
    this.isTyping = true;
    setTimeout(() => {
      this.isTyping = false;
      const reply = this.fakeAssistantReply(content);
      this.messages.push({ role: 'assistant', text: reply, timestamp: new Date() });
      this.conversations[this.activeConversationIndex].lastMessage = reply;
      if (this.conversations[this.activeConversationIndex].title === 'New chat') {
        this.conversations[this.activeConversationIndex].title = content.slice(0, 24) || 'Conversation';
      }
      this.scrollToBottomSoon();
    }, 700);
  }

  private scrollToBottomSoon() {
    setTimeout(() => {
      const el = this.messagesScroll?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }

  private fakeAssistantReply(userText: string): string {
    const canned: string[] = [
      'Got it! Tell me more.',
      'Interesting! What would you like to achieve?',
      'Here’s a thought: break it into smaller steps.',
      'I can help with that. Do you have an example?',
      'Thanks! I’m on it. One sec…'
    ];
    const idx = Math.abs(this.hashCode(userText)) % canned.length;
    return canned[idx];
  }

  private hashCode(s: string) {
    let h = 0;
    for (let i = 0; i < s.length; i++) h = Math.imul(31, h) + s.charCodeAt(i) | 0;
    return h;
  }
}
