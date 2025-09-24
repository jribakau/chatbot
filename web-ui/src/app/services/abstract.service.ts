import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export abstract class AbstractService<TEntity, TFilter> {
  abstract baseUrl: string;

  constructor(protected http: HttpClient) {}

  list(filter: TFilter): Observable<TEntity[]> {
    return this.http.get<TEntity[]>(this.baseUrl, { params: filter as any });
  }

  save(item: TEntity): Observable<TEntity> {
    const id = (item as any).id;
    if (id) {
      return this.http.put<TEntity>(`${this.baseUrl}/${id}`, item);
    } else {
      return this.http.post<TEntity>(this.baseUrl, item);
    }
  }

  delete(id: any): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
