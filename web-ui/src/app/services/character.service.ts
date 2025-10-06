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

}
