import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { AuthService } from '../services/auth.service';
import { SettingsService } from '../services/settings.service';
import { ThemeService } from '../services/theme.service';
import { ToastService } from '../services/toast.service';
import { TeamService } from '../services/team.service';
import { UserService } from '../services/user.service';
import type { Preferences, SessionItem, ThemeMode } from '../models/settings.model';
import type { TeamResponse } from '../models/team.model';
import type { UserDirectoryItem } from '../models/user.model';

type TabKey = 'profile' | 'security' | 'theme' | 'notifications' | 'team';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
  #route = inject(ActivatedRoute);
  #router = inject(Router);
  #auth = inject(AuthService);
  #settings = inject(SettingsService);
  #theme = inject(ThemeService);
  #toast = inject(ToastService);
  #teams = inject(TeamService);
  #users = inject(UserService);

  tab: TabKey = 'profile';
  loading = false;
  error = '';

  profile = {
    fullName: '',
    email: '',
    currentPassword: '',
    avatarColour: '#2563EB',
    bio: ''
  };

  avatarColors: string[] = [
  '#2563EB',
  '#7C3AED',
  '#059669',
  '#F59E0B',
  '#EF4444',
  '#EC4899'
];
  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: ''
  };

  preferences: Preferences = {
    theme: 'LIGHT',
    notifyAssigned: true,
    notifyComment: true,
    notifySubtask: true,
    notifyOverdue: true,
    notifyTeam: true,
    avatarColour: '#2563EB',
    bio: ''
  };

  sessions: SessionItem[] = [];
  teams: TeamResponse[] = [];
  users: UserDirectoryItem[] = [];
  inviteByTeamId: Record<number, number> = {};

  get role(): string {
    return this.#auth.getCurrentUser()?.role || 'MEMBER';
  }

  get canManageTeams(): boolean {
    return this.role === 'ADMIN' || this.role === 'MANAGER';
  }

  ngOnInit(): void {
    this.resolveTabFromRoute();
    this.loadInitial();
  }

  setTab(tab: TabKey): void {
    if (tab === 'team' && !this.canManageTeams) {
      this.#toast.show('You do not have permission to view Team Settings.', 'error');
      this.tab = 'profile';
      this.#router.navigate(['/settings'], { queryParams: { tab: 'profile' } }).catch(() => {});
      return;
    }
    this.tab = tab;
    this.#router.navigate(['/settings'], { queryParams: { tab } }).catch(() => {});
    if (tab === 'security') this.loadSessions();
    if (tab === 'team') this.loadTeams();
  }

  saveProfile(): void {
    this.loading = true;
    this.error = '';
    const payload: any = {
      fullName: this.profile.fullName,
      email: this.profile.email,
      avatarColour: this.profile.avatarColour,
      bio: this.profile.bio
    };
    if (this.profile.currentPassword.trim()) payload.currentPassword = this.profile.currentPassword.trim();

    this.#settings.updateProfile(payload).subscribe({
      next: (me) => {
        this.loading = false;
        this.profile.currentPassword = '';
        this.#auth.updateStoredUser({ fullName: me.fullName, email: me.email, role: me.role });
        this.#toast.show('Profile updated', 'success');
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to update profile';
      }
    });
  }

  savePassword(): void {
    this.loading = true;
    this.error = '';
    this.#settings.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.loading = false;
        this.passwordForm = { currentPassword: '', newPassword: '', confirmNewPassword: '' };
        this.#toast.show('Password updated', 'success');
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to update password';
      }
    });
  }

  setTheme(theme: ThemeMode): void {
    this.preferences.theme = theme;
    this.#theme.setTheme(theme);
    this.#settings.updatePreferences({ theme }).subscribe({
      next: () => {},
      error: () => this.#toast.show('Failed to save theme preference', 'error')
    });
  }

  togglePreference(key: 'notifyAssigned' | 'notifyComment' | 'notifySubtask' | 'notifyOverdue' | 'notifyTeam'): void {
    const value = this.preferences[key];
    this.#settings.updatePreferences({ [key]: value }).subscribe({
      next: () => {},
      error: () => this.#toast.show('Failed to save preference', 'error')
    });
  }

  revokeSession(jti: string): void {
    this.#settings.revokeSession(jti).subscribe({
      next: () => {
        this.loadSessions();
        this.#toast.show('Session revoked', 'success');
      },
      error: () => this.#toast.show('Failed to revoke session', 'error')
    });
  }

  revokeAllOthers(): void {
    this.#settings.revokeAllOtherSessions().subscribe({
      next: () => {
        this.loadSessions();
        this.#toast.show('All other sessions revoked', 'success');
      },
      error: () => this.#toast.show('Failed to revoke sessions', 'error')
    });
  }

  deleteMyAccount(): void {
    const confirmEmail = prompt('Type your email to confirm account deletion');
    if (!confirmEmail) return;
    this.#settings.deleteMyAccount(confirmEmail).subscribe({
      next: () => {
        this.#toast.show('Account deleted', 'success');
        this.#auth.logout();
      },
      error: (err) => this.#toast.show(err?.error?.message || 'Failed to delete account', 'error')
    });
  }

  editTeam(team: TeamResponse): void {
    const name = prompt('Team name', team.name);
    if (!name || !name.trim()) return;
    const description = prompt('Description', team.description || '') ?? '';
    this.#teams.update(team.id, { name: name.trim(), description: description.trim() || undefined }).subscribe({
      next: () => {
        this.loadTeams();
        this.#toast.show('Team updated', 'success');
      },
      error: (err) => this.#toast.show(err?.error?.message || 'Failed to update team', 'error')
    });
  }

  deleteTeam(team: TeamResponse): void {
    if (!confirm(`Delete team "${team.name}"?`)) return;
    this.#teams.delete(team.id).subscribe({
      next: () => {
        this.loadTeams();
        this.#toast.show('Team deleted', 'success');
      },
      error: (err) => this.#toast.show(err?.error?.message || 'Failed to delete team', 'error')
    });
  }

  inviteMember(team: TeamResponse): void {
    const userId = Number(this.inviteByTeamId[team.id] || 0);
    if (!userId) return;
    this.#teams.addMember(team.id, userId).subscribe({
      next: () => {
        this.inviteByTeamId[team.id] = 0;
        this.#toast.show('Member invited', 'success');
      },
      error: (err) => this.#toast.show(err?.error?.message || 'Failed to invite member', 'error')
    });
  }

  private resolveTabFromRoute(): void {
    this.#route.queryParamMap.subscribe((params) => {
      const q = (params.get('tab') || 'profile').toLowerCase();
      const pathTab = this.#route.snapshot.routeConfig?.path === 'settings/team' ? 'team' : q;
      const next = (['profile', 'security', 'theme', 'notifications', 'team'] as TabKey[]).includes(pathTab as TabKey)
        ? (pathTab as TabKey)
        : 'profile';
      if (next === 'team' && !this.canManageTeams) {
        this.#toast.show('You do not have permission to view Team Settings.', 'error');
        this.tab = 'profile';
        return;
      }
      this.tab = next;
      if (this.tab === 'security') this.loadSessions();
      if (this.tab === 'team') this.loadTeams();
    });
  }

  private loadInitial(): void {
    this.loading = true;
    this.#settings.getMe().subscribe({
      next: (me) => {
        this.profile.fullName = me.fullName;
        this.profile.email = me.email;
        this.profile.avatarColour = me.avatarColour || '#2563EB';
        this.profile.bio = me.bio || '';
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to load profile';
      }
    });

    this.#settings.getPreferences().subscribe({
      next: (pref) => {
        this.preferences = pref;
        this.profile.avatarColour = pref.avatarColour || this.profile.avatarColour;
        this.profile.bio = pref.bio || this.profile.bio;
        this.#theme.setTheme(pref.theme || 'LIGHT');
      },
      error: () => {}
    });

    if (this.canManageTeams) {
      this.#users.list().subscribe({ next: (rows) => (this.users = rows || []), error: () => {} });
    }
  }

  private loadSessions(): void {
    this.#settings.getSessions().subscribe({
      next: (rows) => (this.sessions = rows || []),
      error: () => (this.sessions = [])
    });
  }

  private loadTeams(): void {
    if (!this.canManageTeams) return;
    this.#teams.list().subscribe({
      next: (rows) => (this.teams = rows || []),
      error: () => (this.teams = [])
    });
  }
}
