import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Character } from '../../models/character';
import { CharacterService } from '../../services/character.service';
import { JwtService } from '../../services/jwt.service';
import { SharedStateService } from '../../services/shared-state.service';
import { NavbarComponent } from '../navbar/navbar';
import { SidebarAction, SidebarComponent } from '../sidebar/sidebar';

@Component({
    selector: 'app-character',
    standalone: true,
    imports: [FormsModule, NavbarComponent, SidebarComponent],
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

    selectedFile: File | null = null;
    uploadingImage: boolean = false;
    imagePreview: string | null = null;

    currentUsername: string | null = null;

    constructor(
        private characterService: CharacterService,
        private router: Router,
        private jwtService: JwtService,
        private sharedStateService: SharedStateService
    ) { }

    ngOnInit() {
        this.loadCharacters();
        this.loadCurrentUser();
    }

    get sidebarActions(): SidebarAction[] {
        return [
            {
                label: 'New Character',
                handler: () => this.startCreate()
            },
            {
                label: 'Back to Chat',
                handler: () => this.backToChat()
            }
        ];
    }

    loadCurrentUser() {
        this.currentUsername = this.jwtService.getUsername();
    }

    loadCharacters() {
        this.loading = true;
        this.characterService.getCharacters().subscribe(
            (characters: Character[]) => {
                this.characters = characters;
                this.loading = false;

                // Check if there's a shared selected character from the chat page
                const sharedCharacterId = this.sharedStateService.getSelectedCharacterId();
                if (sharedCharacterId) {
                    const character = characters.find(c => c.id === sharedCharacterId);
                    if (character) {
                        // Directly start editing the character
                        this.startEdit(character);
                    }
                } else {
                    // If no character is selected, start creating a new one
                    this.startCreate();
                }
            },
            (error: any) => {
                console.error('Failed to load characters', error);
                this.loading = false;
            }
        );
    }

    selectCharacter(character: Character) {
        this.selectedCharacter = character;
        // Update shared state so chat page knows about this selection
        if (character.id) {
            this.sharedStateService.setSelectedCharacterId(character.id);
        }
    }

    onCharacterSelected(characterId: string) {
        const character = this.characters.find(c => c.id === characterId);
        if (character) {
            this.selectCharacter(character);
            // Automatically start editing when a character is selected
            this.startEdit(character);
        }
    }

    startCreate() {
        this.newCharacter = { customFields: {} };
        this.customFieldsArray = [];
        this.selectedFile = null;
        this.imagePreview = null;
        this.isCreating = true;
        this.isEditing = false;
        this.selectedCharacter = null;
    }

    backToChat() {
        this.router.navigate(['/chat']);
    }

    onLogout() {
        this.jwtService.clearToken();
        this.router.navigate(['/login']);
    }

    toggleSettings() {
        console.log('Settings clicked');
        // Implement settings logic here
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

        this.selectedFile = null;
        this.imagePreview = null;
        this.isEditing = true;
        this.isCreating = false;
    }

    cancelEdit() {
        this.isCreating = false;
        this.isEditing = false;
        this.newCharacter = {};
        this.customFieldsArray = [];
        this.selectedCharacter = null;
        this.selectedFile = null;
        this.imagePreview = null;
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

    onFileSelected(event: Event) {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            const file = input.files[0];

            // Validate file type
            const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
            if (!allowedTypes.includes(file.type)) {
                alert('Invalid file type. Please upload a JPG, PNG, or WEBP image.');
                input.value = '';
                return;
            }

            // Validate file size (5MB max)
            const maxSize = 5 * 1024 * 1024;
            if (file.size > maxSize) {
                alert('File size exceeds 5MB. Please choose a smaller image.');
                input.value = '';
                return;
            }

            this.selectedFile = file;

            // Create preview
            const reader = new FileReader();
            reader.onload = (e: ProgressEvent<FileReader>) => {
                this.imagePreview = e.target?.result as string;
            };
            reader.readAsDataURL(file);
        }
    }

    uploadProfileImage(characterId: string) {
        if (!this.selectedFile) return;

        this.uploadingImage = true;
        this.characterService.uploadProfileImage(characterId, this.selectedFile).subscribe(
            (updatedCharacter: Character) => {
                console.log('Profile image uploaded successfully');
                const index = this.characters.findIndex(c => c.id === updatedCharacter.id);
                if (index !== -1) {
                    this.characters[index] = updatedCharacter;
                }
                if (this.selectedCharacter?.id === updatedCharacter.id) {
                    this.selectedCharacter = updatedCharacter;
                }
                this.selectedFile = null;
                this.imagePreview = null;
                this.uploadingImage = false;
            },
            (error: any) => {
                console.error('Failed to upload profile image', error);
                alert('Failed to upload image. Please try again.');
                this.uploadingImage = false;
            }
        );
    }

    deleteProfileImage(characterId: string) {
        if (!confirm('Are you sure you want to delete this profile image?')) {
            return;
        }

        this.characterService.deleteProfileImage(characterId).subscribe(
            (updatedCharacter: Character) => {
                console.log('Profile image deleted successfully');
                const index = this.characters.findIndex(c => c.id === updatedCharacter.id);
                if (index !== -1) {
                    this.characters[index] = updatedCharacter;
                }
                if (this.selectedCharacter?.id === updatedCharacter.id) {
                    this.selectedCharacter = updatedCharacter;
                }
            },
            (error: any) => {
                console.error('Failed to delete profile image', error);
                alert('Failed to delete image. Please try again.');
            }
        );
    }

    getProfileImageUrl(character: Character, size: 'small' | 'medium' | 'large' = 'medium'): string | null {
        return this.characterService.getProfileImageUrl(character, size);
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

                    // Upload image if selected
                    if (this.selectedFile && character.id) {
                        this.uploadProfileImage(character.id);
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

                    // Upload image if selected
                    if (this.selectedFile && character.id) {
                        this.uploadProfileImage(character.id);
                    }

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
