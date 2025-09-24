import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { AbstractService } from './abstract.service';
import { UserFilter } from '../models/userFilter';

@Injectable({
  providedIn: 'root'
})
export class UserService extends AbstractService<User, UserFilter> {
  override baseUrl = 'http://localhost:8080/api/users'; // Adjust to your backend URL

  constructor(http: HttpClient) {
    super(http);
  }

  login(user: User): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, user);
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {});
  }
}
