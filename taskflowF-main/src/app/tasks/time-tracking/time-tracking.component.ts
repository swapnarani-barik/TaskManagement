import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnDestroy, SimpleChanges, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { TaskTimeLogResponse } from '../../models/time-tracking.model';

@Component({
  selector: 'app-time-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './time-tracking.component.html',
  styleUrls: ['./time-tracking.component.css']
})
export class TimeTrackingComponent implements OnChanges, OnDestroy {
  @Input() taskId!: number;

  #tasks = inject(TaskService);
  #auth = inject(AuthService);

  logs: TaskTimeLogResponse[] = [];
  totalMinutes = 0;
  loading = false;
  error = '';

  timerRunning = false;
  timerStartAt: Date | null = null;
  elapsedSeconds = 0;
  ticker?: Subscription;

  showManual = false;
  manualHours = 0;
  manualMinutes = 0;
  manualDate = this.todayYmd();
  manualNote = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) {
      this.resetTimerState();
      this.loadAll();
    }
  }

  ngOnDestroy(): void {
    this.stopTicker();
  }

  get isViewer(): boolean {
    return (this.#auth.getCurrentUser()?.role ?? 'MEMBER') === 'VIEWER';
  }

  loadAll(): void {
    this.loading = true;
    this.error = '';
    this.#tasks.listTimeLogs(this.taskId).subscribe({
      next: (rows) => {
        this.logs = rows ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load time logs.';
        this.loading = false;
      }
    });

    this.#tasks.getTimeLogTotal(this.taskId).subscribe({
      next: (res) => {
        this.totalMinutes = res?.totalMinutes ?? 0;
      }
    });

    this.#tasks.getActiveTimer(this.taskId).subscribe({
      next: (res) => {
        this.timerRunning = !!res?.running;
        this.timerStartAt = this.parseStartTime(res?.startTime ?? null);
        if (this.timerRunning && !this.timerStartAt) {
          // Keep UI live even if backend timestamp format is non-ISO.
          this.timerStartAt = new Date();
        }
        if (this.timerRunning && this.timerStartAt) {
          this.restartTicker();
        }
      }
    });
  }

  toggleTimer(): void {
    if (this.isViewer) return;
    this.error = '';
    if (!this.timerRunning) {
      this.#tasks.startTimer(this.taskId).subscribe({
        next: (res) => {
          this.timerRunning = true;
          this.timerStartAt = this.parseStartTime(res?.startTime ?? null) ?? new Date();
          this.restartTicker();
        },
        error: (err) => {
          this.error = err?.error?.message || 'Failed to start timer.';
        }
      });
      return;
    }

    this.#tasks.stopTimer(this.taskId).subscribe({
      next: () => {
        this.resetTimerState();
        this.loadAll();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to stop timer.';
      }
    });
  }

  createManual(): void {
    if (this.isViewer) return;
    const total = (this.manualHours * 60) + this.manualMinutes;
    if (total <= 0) {
      this.error = 'Please enter a valid duration.';
      return;
    }

    this.error = '';
    this.#tasks.createManualTimeLog(this.taskId, {
      durationMinutes: total,
      logDate: this.manualDate,
      note: this.manualNote?.trim() || undefined
    }).subscribe({
      next: () => {
        this.manualHours = 0;
        this.manualMinutes = 0;
        this.manualDate = this.todayYmd();
        this.manualNote = '';
        this.showManual = false;
        this.loadAll();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to add manual log.';
      }
    });
  }

  deleteLog(row: TaskTimeLogResponse): void {
    if (!this.canDelete(row)) return;
    this.#tasks.deleteTimeLog(row.id).subscribe({
      next: () => this.loadAll(),
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete log.';
      }
    });
  }

  canDelete(row: TaskTimeLogResponse): boolean {
    if (!row.manual) return false;
    if (this.isViewer) return false;
    const role = this.#auth.getCurrentUser()?.role ?? 'MEMBER';
    const currentUserId = this.#auth.getCurrentUserId();
    return role === 'ADMIN' || role === 'MANAGER' || currentUserId === row.loggedById;
  }

  formatTimer(): string {
    const safeSeconds = Number.isFinite(this.elapsedSeconds) ? this.elapsedSeconds : 0;
    const h = Math.floor(safeSeconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((safeSeconds % 3600) / 60).toString().padStart(2, '0');
    const s = (safeSeconds % 60).toString().padStart(2, '0');
    return `${h}:${m}:${s}`;
  }

  formatMinutes(totalMinutes: number): string {
    if (totalMinutes <= 0) return 'No time logged';
    if (totalMinutes < 60) return `${totalMinutes}m`;
    const h = Math.floor(totalMinutes / 60);
    const m = totalMinutes % 60;
    return `${h}h ${m}m`;
  }

  formatLogDate(isoDate: string): string {
    const d = new Date(isoDate);
    if (Number.isNaN(d.getTime())) return isoDate;
    return d.toLocaleDateString();
  }

  private restartTicker(): void {
    this.stopTicker();
    this.updateElapsed();
    this.ticker = interval(1000).subscribe(() => this.updateElapsed());
  }

  private updateElapsed(): void {
    if (!this.timerStartAt) {
      this.elapsedSeconds = 0;
      return;
    }
    const diffMs = Date.now() - this.timerStartAt.getTime();
    this.elapsedSeconds = Math.max(0, Math.floor(diffMs / 1000));
  }

  private stopTicker(): void {
    this.ticker?.unsubscribe();
    this.ticker = undefined;
  }

  private resetTimerState(): void {
    this.stopTicker();
    this.timerRunning = false;
    this.timerStartAt = null;
    this.elapsedSeconds = 0;
  }

  private todayYmd(): string {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = `${d.getMonth() + 1}`.padStart(2, '0');
    const dd = `${d.getDate()}`.padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private parseStartTime(raw: string | null): Date | null {
    if (!raw) return null;
    const direct = new Date(raw);
    if (!Number.isNaN(direct.getTime())) return direct;

    // Some backends send nanosecond precision (e.g. .123456Z), normalize to ms.
    const normalized = raw.replace(/\.(\d{3})\d+(Z?)$/, '.$1$2');
    const parsed = new Date(normalized);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }
}
