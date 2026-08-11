import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskComment, TaskCommentRequest } from '../.././models/task.model';
import { TaskService } from '../../services/task.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-task-comments',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './task-comments.component.html',
  styleUrls: ['./task-comments.component.css']
})
export class TaskCommentsComponent implements OnChanges {
  @Input() taskId!: number;

  #taskService = inject(TaskService);
  #auth = inject(AuthService);
  #fb = inject(FormBuilder);

  comments: TaskComment[] = [];
  loading = false;
  error = '';

  form = this.#fb.nonNullable.group({
    body: this.#fb.nonNullable.control<string>('', { validators: [Validators.required] })
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) {
      this.loadComments();
    }
  }

  loadComments(): void {
    this.loading = true;
    this.error = '';
    this.#taskService.getComments(this.taskId).subscribe({
      next: (c) => { this.comments = c; this.loading = false; },
      error: () => { this.error = 'Failed to load comments'; this.loading = false; }
    });
  }

  post(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.error = '';
    const req: TaskCommentRequest = this.form.getRawValue();
    this.#taskService.createComment(this.taskId, req).subscribe({
      next: (c) => { this.comments.push(c); this.form.reset({ body: '' }); },
      error: () => { this.error = 'Failed to post comment'; }
    });
  }

  deleteComment(id: number): void {
    this.error = '';
    this.#taskService.deleteComment(id).subscribe({
      next: () => { this.comments = this.comments.filter(c => c.id !== id); },
      error: (err) => { this.error = err?.error?.message ?? 'Failed to delete comment'; }
    });
  }

  getCurrentUserId(): number {
    return this.#auth.getCurrentUserId();
  }

  relativeTime(value: string): string {
    const ts = new Date(value).getTime();
    if (Number.isNaN(ts)) return '';
    const diffSeconds = Math.max(1, Math.floor((Date.now() - ts) / 1000));
    if (diffSeconds < 60) return 'just now';
    const diffMinutes = Math.floor(diffSeconds / 60);
    if (diffMinutes < 60) return `${diffMinutes} minute${diffMinutes === 1 ? '' : 's'} ago`;
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours === 1 ? '' : 's'} ago`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 30) return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;
    const diffMonths = Math.floor(diffDays / 30);
    if (diffMonths < 12) return `${diffMonths} month${diffMonths === 1 ? '' : 's'} ago`;
    const diffYears = Math.floor(diffMonths / 12);
    return `${diffYears} year${diffYears === 1 ? '' : 's'} ago`;
  }
}