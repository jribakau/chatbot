export interface JwtPayload {
  userId: string;
  username: string;
  email: string;
  role: string;
  sub: string;
  iat: number;
  exp: number;
}
