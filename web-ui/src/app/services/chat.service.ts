import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { AbstractService } from "./abstract.service";
import { ChatFilter } from "../models/chatFilter";
import { Chat } from "../models/chat";

@Injectable({ providedIn: 'root' })
export class ChatService extends AbstractService<Chat, ChatFilter> {
    override baseUrl = environment.apiBaseUrl;

    constructor(http: HttpClient) {
        super(http);
    }
}