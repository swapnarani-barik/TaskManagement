export type ThemeMode = 'LIGHT' | 'DARK' | 'SYSTEM';

export interface MeProfile {
  id: number;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';
  avatarColour: string;
  bio?: string | null;
}

export interface Preferences {
  theme: ThemeMode;
  notifyAssigned: boolean;
  notifyComment: boolean;
  notifySubtask: boolean;
  notifyOverdue: boolean;
  notifyTeam: boolean;
  avatarColour: string;
  bio?: string | null;
}

export interface SessionItem {
  jti: string;
  deviceHint?: string | null;
  loginTime: string;
  lastActive: string;
  active: boolean;
}
