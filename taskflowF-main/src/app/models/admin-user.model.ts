export type AdminRole = 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';

export interface AdminTeamBrief {
  id: number;
  name: string;
}

export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  role: AdminRole;
  active: boolean;
  joinedAt: string;
  teams: AdminTeamBrief[];
}

