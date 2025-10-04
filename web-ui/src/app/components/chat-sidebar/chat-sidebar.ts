import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Character } from '../../models/character';

@Component({
  selector: 'app-chat-sidebar',
  imports: [CommonModule],
  templateUrl: './chat-sidebar.html',
  styleUrl: './chat-sidebar.scss'
})
export class ChatSidebar {
  @Input() characters: Character[] = [];
  @Input() selectedCharacterId: string | null = null;
  @Output() characterSelected = new EventEmitter<string>();
  @Output() newChatClicked = new EventEmitter<void>();

  onSelectCharacter(id: string | undefined | null) {
    if (id) {
      this.characterSelected.emit(id);
    }
  }

  onNewChat() {
    this.newChatClicked.emit();
  }
}
