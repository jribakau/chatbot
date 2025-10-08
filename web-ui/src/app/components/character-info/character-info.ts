import { Component, Input } from '@angular/core';
import { Character } from '../../models/character';
import { CharacterService } from '../../services/character.service';

@Component({
  selector: 'app-character-info',
  standalone: true,
  imports: [],
  templateUrl: './character-info.html',
  styleUrl: './character-info.scss'
})
export class CharacterInfo {
  @Input() character?: Character;

  constructor(private characterService: CharacterService) {}

  getInitials(name: string): string {
    if (!name) return '?';
    const words = name.trim().split(/\s+/);
    if (words.length === 1) {
      return name.substring(0, 2).toUpperCase();
    }
    return (words[0][0] + words[words.length - 1][0]).toUpperCase();
  }

  hasCustomFields(): boolean {
    return !!this.character?.customFields && Object.keys(this.character.customFields).length > 0;
  }

  getCustomFieldEntries(): Array<{ key: string; value: string }> {
    if (!this.character?.customFields) return [];
    return Object.entries(this.character.customFields).map(([key, value]) => ({ key, value }));
  }

  formatFieldName(key: string): string {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getProfileImageUrl(character: Character): string | null {
    return this.characterService.getProfileImageUrl(character, 'large');
  }
}
