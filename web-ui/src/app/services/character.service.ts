import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { Character } from "../models/character";
import { CharacterFilter } from "../models/characterFilter";
import { AbstractService } from "./abstract.service";

@Injectable({ providedIn: 'root' })
export class CharacterService extends AbstractService<Character, CharacterFilter> {
  override baseUrl = environment.apiBaseUrl;

  constructor(http: HttpClient) {
    super(http);
  }

  getCharacters(): Observable<Character[]> {
    return this.http.get<Character[]>(`${this.baseUrl}/characters`);
  }

  getCharacter(id: string): Observable<Character> {
    return this.http.get<Character>(`${this.baseUrl}/characters/${id}`);
  }

  createCharacter(character: Partial<Character>): Observable<Character> {
    return this.http.post<Character>(`${this.baseUrl}/characters`, character);
  }

  updateCharacter(id: string, character: Partial<Character>): Observable<Character> {
    return this.http.put<Character>(`${this.baseUrl}/characters/${id}`, character);
  }

  deleteCharacter(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/characters/${id}`);
  }

  uploadProfileImage(id: string, file: File): Observable<Character> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Character>(`${this.baseUrl}/characters/${id}/profile-image`, formData);
  }

  deleteProfileImage(id: string): Observable<Character> {
    return this.http.delete<Character>(`${this.baseUrl}/characters/${id}/profile-image`);
  }

  /**
   * Get the full URL for a character's profile image
   * @param character - The character object
   * @param size - The desired image size (small, medium, or large)
   * @returns The full URL to the profile image, or null if no image exists
   */
  getProfileImageUrl(character: Character, size: 'small' | 'medium' | 'large' = 'medium'): string | null {
    const imageMap = {
      small: character.profileImageSmall,
      medium: character.profileImageMedium,
      large: character.profileImageLarge
    };
    const imagePath = imageMap[size];
    if (!imagePath) {
      return null;
    }
    // Convert relative path to absolute URL using the API base URL
    return `${this.baseUrl.replace('/api', '')}${imagePath}`;
  }
}
