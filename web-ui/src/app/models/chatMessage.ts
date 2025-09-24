import { MessageRoleEnum } from "../enums/messageRoleEnum";
import { Resource } from "./resource";

export interface ChatMessage extends Resource {
  role: MessageRoleEnum;
  content: string;
  timestamp?: Date;
}
