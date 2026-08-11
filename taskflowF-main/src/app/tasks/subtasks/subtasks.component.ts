import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SubtaskResponse } from '../../models/subtask.model';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-subtasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subtasks.component.html',
  styleUrls: ['./subtasks.component.css']
})
export class SubtasksComponent implements OnChanges {
  @Input() taskId!: number;

  #tasks = inject(TaskService);
  #auth = inject(AuthService);

  subtasks: SubtaskResponse[] = [];
  loading = false;
  error = '';
  newTitle = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) {
      this.load();
    }
  }

  get isViewer(): boolean {
    return (this.#auth.getCurrentUser()?.role ?? 'MEMBER') === 'VIEWER';
  }

  get doneCount(): number {
    return this.subtasks.filter(s => s.complete).length;
  }

  get allDone(): boolean {
    return this.subtasks.length > 0 && this.doneCount === this.subtasks.length;
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.#tasks.listSubtasks(this.taskId).subscribe({
      next: (rows) => {
        this.subtasks = rows ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load subtasks.';
        this.loading = false;
      }
    });
  }

  add(): void {
    if (this.isViewer) return;
    const title = this.newTitle.trim();
    if (!title) return;
    this.error = '';
    this.#tasks.createSubtask(this.taskId, { title }).subscribe({
      next: (created) => {
        this.subtasks = [...this.subtasks, created];
        this.newTitle = '';
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to create subtask.';
      }
    });
  }

  toggle(item: SubtaskResponse): void {
    if (this.isViewer) return;
    this.error = '';
    this.#tasks.toggleSubtask(item.id).subscribe({
      next: (updated) => {
        this.subtasks = this.subtasks.map(s => s.id === updated.id ? updated : s);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update subtask.';
      }
    });
  }

  remove(item: SubtaskResponse): void {
    if (!this.canDelete(item)) return;
    this.error = '';
    this.#tasks.deleteSubtask(item.id).subscribe({
      next: () => {
        this.subtasks = this.subtasks.filter(s => s.id !== item.id);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete subtask.';
      }
    });
  }

  canDelete(item: SubtaskResponse): boolean {
    const role = this.#auth.getCurrentUser()?.role ?? 'MEMBER';
    const currentUserId = this.#auth.getCurrentUserId();
    return role === 'ADMIN' || role === 'MANAGER' || currentUserId === item.createdById;
  }
}

