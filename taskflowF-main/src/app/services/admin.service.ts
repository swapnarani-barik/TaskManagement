import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import type { AdminRole, AdminUser } from '../models/admin-user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/admin`;

  listUsers() {
    return this.#http.get<AdminUser[]>(`${this.#base}/users`);
  }

  changeRole(userId: number, role: AdminRole) {
    return this.#http.patch<void>(`${this.#base}/users/${userId}/role`, { role });
  }

  changeStatus(userId: number, active: boolean) {
    // Backend accepts both {active} and {isActive}
    return this.#http.patch<void>(`${this.#base}/users/${userId}/status`, { active });
  }

  deleteUser(userId: number) {
    return this.#http.delete<void>(`${this.#base}/users/${userId}`);
  }
}
