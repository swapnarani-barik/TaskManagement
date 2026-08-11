export interface SubtaskResponse {
  id: number;
  taskId: number;
  title: string;
  complete: boolean;
  assignedToId?: number | null;
  assignedToName?: string | null;
  createdById: number;
  createdByName: string;
  createdAt: string;
  completedAt?: string | null;
}

export interface SubtaskSummaryResponse {
  total: number;
  completed: number;
}

export interface SubtaskCreateRequest {
  title: string;
  assignedTo?: number | null;
}

