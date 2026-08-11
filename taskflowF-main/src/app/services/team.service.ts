import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import type { TeamResponse } from '../models/team.model';
import type { TeamDetailResponse } from '../models/team-detail.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/teams`;

  list() {
    return this.#http.get<TeamResponse[]>(this.#base);
  }

  get(id: number) {
    return this.#http.get<TeamDetailResponse>(`${this.#base}/${id}`);
  }

  create(payload: { name: string; description?: string; managerId?: number; memberIds?: number[] }) {
    return this.#http.post<TeamResponse>(this.#base, payload);
  }

  update(teamId: number, payload: { name: string; description?: string; managerId?: number }) {
    return this.#http.put<TeamResponse>(`${this.#base}/${teamId}`, payload);
  }

  delete(teamId: number) {
    return this.#http.delete<void>(`${this.#base}/${teamId}`);
  }

  addMember(teamId: number, userId: number) {
    return this.#http.post<void>(`${this.#base}/${teamId}/members`, { userId });
  }

  removeMember(teamId: number, userId: number) {
    return this.#http.delete<void>(`${this.#base}/${teamId}/members/${userId}`);
  }
}
