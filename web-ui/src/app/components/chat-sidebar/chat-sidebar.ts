import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Character } from '../../models/character';
import { Chat } from '../../models/chat';
import { CharacterService } from '../../services/character.service';

@Component({
  selector: 'app-chat-sidebar',
  imports: [],
  templateUrl: './chat-sidebar.html',
  styleUrl: './chat-sidebar.scss'
})
export class ChatSidebar {
  @Input() characters: Character[] = [];
  @Input() selectedCharacterId: string | null = null;
  @Input() pastChatsByCharacter: Record<string, Chat[]> = {};
  @Output() characterSelected = new EventEmitter<string>();
  @Output() newCharacter = new EventEmitter<void>();
  @Output() newChatClicked = new EventEmitter<void>();
  @Output() pastChatSelected = new EventEmitter<Chat>();
  @Output() loadPastChats = new EventEmitter<string>();

  expandedCharacterId: string | null = null;

  constructor(private characterService: CharacterService) { }

  onSelectCharacter(id: string | undefined | null) {
    if (id) {
      this.characterSelected.emit(id);
    }
  }

  onNewChat() {
    this.newChatClicked.emit();
  }

  onNewCharacter() {
    this.newCharacter.emit();
  }

  togglePastChats(event: Event, characterId: string | undefined | null) {
    event.stopPropagation();
    if (!characterId) return;

    if (this.expandedCharacterId === characterId) {
      this.expandedCharacterId = null;
    } else {
      this.expandedCharacterId = characterId;
      if (!this.pastChatsByCharacter[characterId] || this.pastChatsByCharacter[characterId].length === 0) {
        this.loadPastChats.emit(characterId);
      }
    }
  }

  onSelectPastChat(event: Event, chat: Chat) {
    event.stopPropagation();
    this.pastChatSelected.emit(chat);
    this.expandedCharacterId = null;
  }

  getPastChats(characterId: string | undefined | null): Chat[] {
    if (!characterId) return [];
    return this.pastChatsByCharacter[characterId] || [];
  }

  isExpanded(characterId: string | undefined | null): boolean {
    return characterId === this.expandedCharacterId;
  }

  getChatPreview(chat: Chat): string {
    if (chat.messageList && chat.messageList.length > 0) {
      const lastMessage = chat.messageList[chat.messageList.length - 1];
      return lastMessage.content || 'Empty chat';
    }
    return 'New chat';
  }

  getChatDate(chat: Chat): string {
    if (chat.updatedAt) {
      return new Date(chat.updatedAt).toLocaleDateString();
    }
    if (chat.createdAt) {
      return new Date(chat.createdAt).toLocaleDateString();
    }
    return '';
  }

  getProfileImageUrl(character: Character): string | null {
    return this.characterService.getProfileImageUrl(character, 'small');
  }

  getInitials(name: string): string {
    if (!name) return '?';
    return name.slice(0, 1).toUpperCase();
  }
}
