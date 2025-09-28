import { Message } from "./message";
import { Resource } from "./resource";

export interface Chat extends Resource {
    characterId?: string;
    messageList: Array<Message>;
}
