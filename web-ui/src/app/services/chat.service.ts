import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { AbstractService } from "./abstract.service";
import { ChatFilter } from "../models/chatFilter";
import { Chat } from "../models/chat";
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class ChatService extends AbstractService<Chat, ChatFilter> {
    override baseUrl = `${environment.apiBaseUrl}/chat`;

    constructor(http: HttpClient) {
        super(http);
    }

    createChat(payload: Partial<Chat>): Observable<Chat> {
        return this.save(payload as Chat);
    }

    getLatestChat(characterId: string, ownerId: string): Observable<Chat> {
        return this.http.get<Chat>(`${this.baseUrl}/latest`, {
            params: { characterId, ownerId }
        });
    }

    getChatById(id: string): Observable<Chat> {
        return this.http.get<Chat>(`${this.baseUrl}/${id}`);
    }

    getChatsByCharacter(characterId: string, ownerId: string): Observable<Chat[]> {
        return this.http.get<Chat[]>(`${this.baseUrl}`, {
            params: { characterId, ownerId }
        });
    }
}
