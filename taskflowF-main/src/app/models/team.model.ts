export interface TeamResponse {
  id: number;
  name: string;
  description?: string | null;
  managerId: number;
  managerName: string;
  memberCount: number;
  activeTasksCount: number;
  memberInitials?: string[];
}
