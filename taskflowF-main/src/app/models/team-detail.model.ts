export interface TeamMemberRow {
  userId: number;
  fullName: string;
  email: string;
  role: string;
  tasksAssigned: number;
  joinedAt: string;
}

export interface TeamDetailResponse {
  id: number;
  name: string;
  description?: string | null;
  managerId: number;
  managerName: string;
  members: TeamMemberRow[];
}

