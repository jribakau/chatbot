import { CommonModule } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Character } from '../../models/character';
import { Message } from '../../models/message';

@Component({
  selector: 'app-chat-pane',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-pane.html',
  styleUrl: './chat-pane.scss'
})
export class ChatPane implements AfterViewChecked {
  @ViewChild('messagesScroll') messagesScroll?: ElementRef<HTMLDivElement>;

  @Input() activeCharacter?: Character;
  @Input() messages: Message[] = [];
  @Input() isTyping = false;
  @Input() draft = '';

  @Output() draftChange = new EventEmitter<string>();
  @Output() sendMessage = new EventEmitter<string>();
  @Output() logout = new EventEmitter<void>();
  @Output() clearChat = new EventEmitter<void>();
  @Output() settingsClicked = new EventEmitter<void>();

  private shouldScroll = false;

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  onDraftChange(value: string) {
    this.draftChange.emit(value);
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

    this.sendMessage.emit(content);
    this.draft = '';
    this.draftChange.emit('');
    this.shouldScroll = true;
  }

  onLogout() {
    this.logout.emit();
  }

  onClearChat() {
    this.clearChat.emit();
  }

  onSettingsClicked() {
    this.settingsClicked.emit();
  }

  private scrollToBottom() {
    setTimeout(() => {
      const el = this.messagesScroll?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }
}
