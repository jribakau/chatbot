import { Resource } from "./resource";

export interface Character extends Resource {
  name: string;
  description?: string;
  systemPrompt?: string;
  shortGreeting?: string;
  customFields?: { [key: string]: string };
}
