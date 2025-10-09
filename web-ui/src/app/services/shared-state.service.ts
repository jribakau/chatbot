import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Service to maintain shared state across components, particularly
 * the selected character ID when navigating between chat and character pages
 */
@Injectable({
  providedIn: 'root'
})
export class SharedStateService {
  private selectedCharacterIdSubject = new BehaviorSubject<string | null>(null);

  /**
   * Observable stream of the currently selected character ID
   */
  selectedCharacterId$: Observable<string | null> = this.selectedCharacterIdSubject.asObservable();

  /**
   * Sets the currently selected character ID
   */
  setSelectedCharacterId(characterId: string | null): void {
    this.selectedCharacterIdSubject.next(characterId);
  }

  /**
   * Gets the current selected character ID (synchronous)
   */
  getSelectedCharacterId(): string | null {
    return this.selectedCharacterIdSubject.value;
  }

  /**
   * Clears the selected character ID
   */
  clearSelectedCharacterId(): void {
    this.selectedCharacterIdSubject.next(null);
  }
}
