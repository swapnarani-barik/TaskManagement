

import { Component, EventEmitter, Input, Output, DestroyRef, inject, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Priority, TaskRequest, TaskResponse, TaskStatus, UserResponse } from '../.././/models/task.model';
import { TaskCommentsComponent } from '../task-comments/task-comments.component';
import { TaskAttachmentsComponent } from '../task-attachments/task-attachments.component';
import { SubtasksComponent } from '../subtasks/subtasks.component';
import { TimeTrackingComponent } from '../time-tracking/time-tracking.component';
import { TaskService } from '../../services/task.service';

@Component({
  standalone: true,
  selector: 'app-task-form',
  imports: [CommonModule, ReactiveFormsModule, SubtasksComponent, TimeTrackingComponent, TaskCommentsComponent, TaskAttachmentsComponent],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  /** If a task is passed, we are editing */
  @Input() task: TaskResponse | null = null;

  /** Optional audit metadata for the “Last modified” block (edit view) */
  @Input() audit?: {
    statusChanged?: { from: TaskStatus; to: TaskStatus; on: string };
    dueDateChanged?: { from: string; to: string };
  };

  @Input() loading = false;

  @Output() saved = new EventEmitter<TaskRequest>();
  @Output() cancel = new EventEmitter<void>();
  @Output() requestDelete = new EventEmitter<void>(); // used by Danger Zone button (edit only)

  // derived
  get isEdit(): boolean { return !!this.task; }

  #fb = inject(FormBuilder);
  #taskService = inject(TaskService);

  /**
   * Use Non-Nullable FormBuilder so control values are never null.
   * This fixes the 'TaskStatus | null' type in template.
   */
  #nnfb = this.#fb.nonNullable;

  form = this.#nnfb.group({
    title: this.#nnfb.control<string>('', { validators: [Validators.required] }),
    description: this.#nnfb.control<string>(''),
    dueDate: this.#nnfb.control<string>('', { validators: [Validators.required] }), // yyyy-MM-dd
    status: this.#nnfb.control<TaskStatus>('TODO'),
    priority: this.#nnfb.control<Priority>('MEDIUM'),
    assignedToId: this.#nnfb.control<number | null>(null)
  });

  users: UserResponse[] = [];

  ngOnInit(): void {
    this.#taskService.listUsers().subscribe(u => this.users = u);
    if (this.task) {
      const { title, description, dueDate, status, priority, assignedToId } = this.task;
      const ymd = this.toYMD(dueDate);
      this.form.patchValue({
        title,
        description: description ?? '',
        dueDate: ymd,
        status,
        priority,
        assignedToId: assignedToId ?? null
      });
    }
  }

  // Converts ISO or Y-M-D to yyyy-MM-dd for <input type="date">
  private toYMD(d: string): string {
    if (/^\d{4}-\d{2}-\d{2}$/.test(d)) return d;
    const date = new Date(d);
    if (Number.isNaN(date.getTime())) return '';
    const mm = `${date.getMonth() + 1}`.padStart(2, '0');
    const dd = `${date.getDate()}`.padStart(2, '0');
    return `${date.getFullYear()}-${mm}-${dd}`;
  }

  label(s: TaskStatus): string {
    return s === 'IN_PROGRESS' ? 'In Progress' : (s === 'DONE' ? 'Done' : 'To-Do');
  }

  onSubmit(): void {
    if (this.loading || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload: TaskRequest = this.form.getRawValue();
    this.saved.emit(payload);
  }
}
