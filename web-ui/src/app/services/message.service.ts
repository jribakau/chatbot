import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Message } from '../models/message';
import { AbstractService } from './abstract.service';
import { environment } from '../../environments/environment';
import { MessageFilter } from '../models/messageFilter';

@Injectable({ providedIn: 'root' })
export class MessageService extends AbstractService<Message, MessageFilter> {
  override baseUrl = environment.apiBaseUrl;

  constructor(http: HttpClient) {
    super(http);
  }

  sendChatMessage(chatId: string, characterId: string, history: Message[], userMessage: string): Observable<Message> {
    const payload = {
      chatId: chatId,
      characterId: characterId,
      history: history,
      userMessage: userMessage
    };
    return this.http.post<Message>(`${this.baseUrl}/message`, payload);
  }

  updateMessage(message: Message): Observable<Message> {
    return this.http.put<Message>(`${this.baseUrl}/message/${message.id}`, message);
  }

}
