import { Resource } from "./resource";

export interface User extends Resource {
  username?: string;
  email?: string;
  passwordHash?: string;
}
