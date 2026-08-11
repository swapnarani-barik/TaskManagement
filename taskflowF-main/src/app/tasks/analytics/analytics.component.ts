import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { AnalyticsPanelComponent } from '../dashboard/analytics-panel/analytics-panel.component';
import { TaskService } from '../../services/task.service';
import { TaskSummaryResponse } from '../../models/task.model';
import { finalize, timeout } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-analytics',
  imports: [CommonModule, NavbarComponent, AnalyticsPanelComponent],
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit {
  #tasks = inject(TaskService);
  #router = inject(Router);

  loading = false;
  error = '';
  summary: TaskSummaryResponse | null = null;

  ngOnInit(): void {
    this.fetchSummary();
  }

  fetchSummary(): void {
    this.loading = true;
    this.error = '';
    this.#tasks.summary().pipe(
      timeout(8000),
      finalize(() => { this.loading = false; })
    ).subscribe({
      next: (res) => {
        this.summary = res;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load analytics.';
      }
    });
  }

  backToDashboard(): void {
    this.#router.navigate(['/dashboard']);
  }
}