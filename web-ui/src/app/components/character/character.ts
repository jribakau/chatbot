import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Character } from '../../models/character';
import { CharacterService } from '../../services/character.service';

@Component({
    selector: 'app-character',
    standalone: true,
    imports: [FormsModule],
    templateUrl: './character.html',
    styleUrls: ['./character.scss']
})
export class CharacterComponent implements OnInit {
    characters: Character[] = [];
    selectedCharacter: Character | null = null;
    newCharacter: Partial<Character> = {};
    isCreating: boolean = false;
    isEditing: boolean = false;
    loading: boolean = false;

    customFieldsArray: { key: string, value: string }[] = [];
    newFieldKey: string = '';
    newFieldValue: string = '';

    constructor(
        private characterService: CharacterService,
        private router: Router
    ) { }

    ngOnInit() {
        this.loadCharacters();
    }

    loadCharacters() {
        this.loading = true;
        this.characterService.getCharacters().subscribe(
            (characters: Character[]) => {
                this.characters = characters;
                this.loading = false;
            },
            (error: any) => {
                console.error('Failed to load characters', error);
                this.loading = false;
            }
        );
    }

    selectCharacter(character: Character) {
        this.selectedCharacter = character;
        this.isCreating = false;
        this.isEditing = false;
    }

    startCreate() {
        this.newCharacter = { customFields: {} };
        this.customFieldsArray = [];
        this.isCreating = true;
        this.isEditing = false;
        this.selectedCharacter = null;
    }

    backToChat() {
        this.router.navigate(['/chat']);
    }

    startEdit(character: Character) {
        this.selectedCharacter = character;
        this.newCharacter = { ...character };

        this.customFieldsArray = [];
        if (character.customFields) {
            this.customFieldsArray = Object.entries(character.customFields).map(([key, value]) => ({
                key,
                value
            }));
        }

        this.isEditing = true;
        this.isCreating = false;
    }

    cancelEdit() {
        this.isCreating = false;
        this.isEditing = false;
        this.newCharacter = {};
        this.customFieldsArray = [];
        this.selectedCharacter = null;
    }

    addCustomField() {
        if (this.newFieldKey.trim()) {
            this.customFieldsArray.push({
                key: this.newFieldKey.trim(),
                value: this.newFieldValue.trim()
            });
            this.newFieldKey = '';
            this.newFieldValue = '';
        }
    }

    removeCustomField(index: number) {
        this.customFieldsArray.splice(index, 1);
    }

    onCreate() {
        if (this.customFieldsArray.length > 0) {
            this.newCharacter.customFields = {};
            this.customFieldsArray.forEach(field => {
                if (field.key.trim()) {
                    this.newCharacter.customFields![field.key] = field.value;
                }
            });
        }

        if (this.isEditing && this.selectedCharacter?.id) {
            this.characterService.updateCharacter(this.selectedCharacter.id, this.newCharacter).subscribe(
                (character: Character) => {
                    console.log('Character updated successfully', character);
                    const index = this.characters.findIndex(c => c.id === character.id);
                    if (index !== -1) {
                        this.characters[index] = character;
                    }
                    this.isEditing = false;
                    this.newCharacter = {};
                    this.customFieldsArray = [];
                    this.selectedCharacter = character;
                },
                (error: any) => {
                    console.error('Failed to update character', error);
                }
            );
        } else {
            this.characterService.createCharacter(this.newCharacter).subscribe(
                (character: Character) => {
                    console.log('Character created successfully', character);
                    this.characters.push(character);
                    this.isCreating = false;
                    this.newCharacter = {};
                    this.customFieldsArray = [];
                    this.selectedCharacter = character;
                },
                (error: any) => {
                    console.error('Failed to create character', error);
                }
            );
        }
    }

    onDelete(characterId: string) {
        if (confirm('Are you sure you want to delete this character?')) {
            this.characterService.deleteCharacter(characterId).subscribe(
                () => {
                    console.log('Character deleted successfully');
                    this.characters = this.characters.filter(c => c.id !== characterId);
                    if (this.selectedCharacter?.id === characterId) {
                        this.selectedCharacter = null;
                        this.isEditing = false;
                    }
                },
                (error: any) => {
                    console.error('Failed to delete character', error);
                    alert('Failed to delete character. Please try again.');
                }
            );
        }
    }

    getCustomFieldsArray(customFields?: { [key: string]: string }): { key: string, value: string }[] {
        if (!customFields) return [];
        return Object.entries(customFields).map(([key, value]) => ({ key, value }));
    }
}
