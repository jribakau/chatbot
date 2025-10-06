import { Injectable } from '@angular/core';
import { JwtPayload } from '../models/jwtPayload';

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  /**
   * Decode a JWT token and return the payload
   */
  decodeToken(token: string): JwtPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      const payload = parts[1];
      const decoded = this.base64UrlDecode(payload);
      return JSON.parse(decoded) as JwtPayload;
    } catch (error) {
      console.error('Failed to decode JWT token', error);
      return null;
    }
  }

  /**
   * Get the stored token from storage
   */
  getToken(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  }

  /**
   * Get user ID from the token
   */
  getUserId(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const payload = this.decodeToken(token);
    return payload?.userId || null;
  }

  /**
   * Get username from the token
   */
  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const payload = this.decodeToken(token);
    return payload?.username || null;
  }

  /**
   * Get email from the token
   */
  getEmail(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const payload = this.decodeToken(token);
    return payload?.email || null;
  }

  /**
   * Get user role from the token
   */
  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const payload = this.decodeToken(token);
    return payload?.role || null;
  }

  /**
   * Check if the token is expired
   */
  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    const payload = this.decodeToken(token);
    if (!payload || !payload.exp) return true;

    const expirationDate = new Date(payload.exp * 1000);
    return expirationDate < new Date();
  }

  /**
   * Get the full decoded payload
   */
  getTokenPayload(): JwtPayload | null {
    const token = this.getToken();
    if (!token) return null;

    return this.decodeToken(token);
  }

  /**
   * Clear the token from storage
   */
  clearToken(): void {
    if (typeof window === 'undefined') return;

    localStorage.removeItem('authToken');
    sessionStorage.removeItem('authToken');
  }

  /**
   * Base64 URL decode helper
   */
  private base64UrlDecode(str: string): string {
    let output = str.replace(/-/g, '+').replace(/_/g, '/');
    switch (output.length % 4) {
      case 0:
        break;
      case 2:
        output += '==';
        break;
      case 3:
        output += '=';
        break;
      default:
        throw new Error('Invalid base64 string');
    }
    return decodeURIComponent(
      atob(output)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
  }
}

