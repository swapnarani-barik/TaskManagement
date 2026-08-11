import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import type { MeProfile, Preferences, SessionItem, ThemeMode } from '../models/settings.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/users/me`;

  getMe() {
    return this.#http.get<MeProfile>(this.#base);
  }

  updateProfile(payload: {
    fullName: string;
    email: string;
    currentPassword?: string;
    avatarColour?: string;
    bio?: string;
  }) {
    return this.#http.patch<MeProfile>(`${this.#base}/profile`, payload);
  }

  changePassword(payload: { currentPassword: string; newPassword: string; confirmNewPassword: string }) {
    return this.#http.patch<void>(`${this.#base}/password`, payload);
  }

  getPreferences() {
    return this.#http.get<Preferences>(`${this.#base}/preferences`);
  }

  updatePreferences(payload: {
    theme?: ThemeMode;
    notifyAssigned?: boolean;
    notifyComment?: boolean;
    notifySubtask?: boolean;
    notifyOverdue?: boolean;
    notifyTeam?: boolean;
  }) {
    return this.#http.patch<Preferences>(`${this.#base}/preferences`, payload);
  }

  deleteMyAccount(confirmEmail: string) {
    return this.#http.request<void>('delete', this.#base, {
      body: { confirmEmail }
    });
  }

  getSessions() {
    return this.#http.get<SessionItem[]>(`${this.#base}/sessions`);
  }

  revokeSession(jti: string) {
    return this.#http.delete<void>(`${this.#base}/sessions/${jti}`);
  }

  revokeAllOtherSessions() {
    return this.#http.delete<void>(`${this.#base}/sessions`);
  }
}
