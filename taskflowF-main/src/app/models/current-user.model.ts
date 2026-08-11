import type { UserRole } from './role.model';

export interface CurrentUser {
  userId: number;
  fullName: string;
  email: string;
  role: UserRole;
}

