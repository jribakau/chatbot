import { MessageRoleEnum } from "../enums/messageRoleEnum";
import { Chat } from "./chat";
import { Resource } from "./resource";

export interface Message extends Resource {
  role: MessageRoleEnum;
  content: string;
  timestamp?: Date;
}
