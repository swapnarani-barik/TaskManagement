export interface ActiveTimerResponse {
  running: boolean;
  startTime?: string | null;
}

export interface TaskTimeLogResponse {
  id: number;
  taskId: number;
  loggedById: number;
  loggedByName: string;
  durationMinutes: number;
  logDate: string;
  note?: string | null;
  manual: boolean;
  createdAt: string;
}

export interface TimeTotalResponse {
  totalMinutes: number;
}

export interface ManualTimeLogRequest {
  durationMinutes: number;
  logDate: string;
  note?: string;
}

