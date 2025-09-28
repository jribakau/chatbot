import { Message } from "./message";
import { Resource } from "./resource";

export interface Chat extends Resource {
    userId: string;
    characterId?: string;
    messageList: Array<Message>;
}
