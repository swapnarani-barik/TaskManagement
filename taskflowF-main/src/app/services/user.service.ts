import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import type { UserDirectoryItem } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/users`;

  list() {
    return this.#http.get<UserDirectoryItem[]>(this.#base);
  }
}

