import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessage } from '../models/chatMessage';
import { AbstractService } from './abstract.service';
import { ChatMessageFilter } from '../models/chatMessageFilter';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService extends AbstractService<ChatMessage, ChatMessageFilter> {
  override baseUrl = environment.apiBaseUrl;

  constructor(http: HttpClient) {
    super(http);
  }

  sendChatMessage(characterId: string, history: ChatMessage[], userMessage: string): Observable<ChatMessage> {
    const payload = {
      characterId: characterId,
      history: history,
      userMessage: userMessage
    };
    return this.http.post<ChatMessage>(`${this.baseUrl}/chat`, payload);
  }

}
