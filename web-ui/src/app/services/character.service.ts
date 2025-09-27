import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Character } from "../models/character";
import { ChatMessage } from "../models/chatMessage";
import { ChatMessageFilter } from "../models/chatMessageFilter";
import { AbstractService } from "./abstract.service";
import { CharacterFilter } from "../models/characterFilter";

@Injectable({ providedIn: 'root' })
export class CharacterService extends AbstractService<Character, CharacterFilter> {
  override baseUrl = 'http://localhost:8080/api';

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

}