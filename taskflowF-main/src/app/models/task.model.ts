export type TaskStatus ='ALL_TASK'| 'TODO' | 'IN_PROGRESS' | 'DONE';
export type Priority=  'HIGH' | 'MEDIUM' | 'LOW';
// export interface TaskResponse {
//   id: number;
//   title: string;
//   description?: string | null;
//   dueDate: string;     // yyyy-mm-dd
//   status: TaskStatus;
//   createdAt: string;
//   updatedAt?: string | null;
// }

// export interface TaskRequest {
//   title: string;
//   description?: string | null;
//   dueDate: string;
//   status: TaskStatus;
// }
export interface TaskResponse {
  id: number;
  title: string;
  description?: string | null;
  dueDate: string;  
  status: TaskStatus;
  priority: Priority;
  ownerId: number;
  ownerName: string;
  assignedToId?: number | null;
  assignedToName?: string | null;
  teamId?: number | null;
  teamName?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface TaskRequest {
  title: string;
  description?: string | null;
  dueDate: string;
  status: TaskStatus;
  priority?: Priority;
  assignedToId?: number | null;
}

export interface TaskComment {
  id: number;
  taskId: number;
  authorId: number;
  authorName: string;
  body: string;
  createdAt: string;
}

export interface TaskCommentRequest {
  body: string;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email?: string;
  role?: string;
}

export interface TaskSummaryResponse {
  totalTasks: number;
  byStatus: {
    todo: number;
    inProgress: number;
    done: number;
  };
  byPriority: {
    high: number;
    medium: number;
    low: number;
  };
  completionRate: number;
  overdueCount: number;
  tasksThisWeek: number;
  dueToday: number;
}

export type ActivityActionCode =
  | 'COMMENT_ADDED'
  | 'TASK_STATUS_CHANGED'
  | 'TASK_ASSIGNED'
  | 'TASK_CREATED'
  | 'TASK_PRIORITY_CHANGED'
  | 'TASK_DELETED';

export interface ActivityLogResponse {
  id: number;
  taskId?: number | null;
  actorId: number;
  actorName: string;
  actionCode: ActivityActionCode;
  message: string;
  createdAt: string;
}
