import { MessageRoleEnum } from "../enums/messageRoleEnum";
import { Resource } from "./resource";

export interface Message extends Resource {
  role: MessageRoleEnum;
  content: string;
  timestamp?: Date;
  versions?: string[];
  currentVersionIndex?: number;
}
