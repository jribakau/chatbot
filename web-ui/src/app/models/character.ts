import { Resource } from "./resource";

export interface Character extends Resource {
  name: string;
  description?: string;
  systemPrompt?: string;
  shortGreeting?: string;
  profileImageSmall?: string;
  profileImageMedium?: string;
  profileImageLarge?: string;
  customFields?: { [key: string]: string };
}
