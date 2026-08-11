import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { TeamService } from '../services/team.service';
import { TaskService } from '../services/task.service';
import { TaskResponse } from '../models/task.model';

@Component({
  selector: 'app-team-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <section class="page">
      <a class="back" routerLink="/teams">← Back</a>

      <div class="card" *ngIf="loading">Loading…</div>
      <div class="card error" *ngIf="error">{{ error }}</div>

      <ng-container *ngIf="!loading && !error && team">
        <h2>{{ team.name }}</h2>
        <p class="muted">{{ team.description || '—' }}</p>

        <div class="card">
          <div><strong>Manager:</strong> {{ team.managerName }}</div>
        </div>

        <h3>Members</h3>
        <div class="card" *ngFor="let m of (team.members || [])">
          <div class="row">
            <div class="name">{{ m.fullName }}</div>
            <div class="pill">{{ m.role }}</div>
          </div>
          <div class="muted small">{{ m.email }}</div>
        </div>

        <h3>Team Tasks</h3>
        <div class="card" *ngFor="let t of teamTasks">
          <div class="row">
            <div class="name">{{ t.title }}</div>
            <div class="pill">{{ t.status }}</div>
          </div>
          <div class="muted small">Due: {{ t.dueDate }} • Priority: {{ t.priority }}</div>
        </div>
        <div class="card" *ngIf="teamTasks.length === 0">
          <div class="muted">No tasks assigned to this team.</div>
        </div>
      </ng-container>
    </section>
  `,
  styles: [
    `
      .page{padding:18px 22px}
      .back{display:inline-block;margin-bottom:10px;text-decoration:none;color:inherit;opacity:.85}
      .muted{opacity:.75;margin:6px 0 12px}
      .muted.small{font-size:12px;margin:6px 0 0}
      .card{padding:12px 14px;border:1px solid rgba(255,255,255,.08);border-radius:12px;background:rgba(255,255,255,.02);margin:10px 0}
      .card.error{border-color:rgba(239,68,68,.35)}
      .row{display:flex;align-items:center;justify-content:space-between;gap:10px}
      .name{font-weight:700}
      .pill{font-size:12px;padding:4px 10px;border-radius:999px;border:1px solid rgba(255,255,255,.12);opacity:.9}
    `
  ]
})
export class TeamDetailComponent implements OnInit {
  #route = inject(ActivatedRoute);
  #teams = inject(TeamService);
  #tasks = inject(TaskService);

  team: any = null;
  teamTasks: TaskResponse[] = [];
  loading = false;
  error = '';

  ngOnInit(): void {
    const id = Number(this.#route.snapshot.paramMap.get('id') ?? 0) || 0;
    this.loading = true;
    this.#teams.get(id).subscribe({
      next: (res) => {
        this.team = res;
        this.loading = false;
        this.loadTeamTasks(id);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load team.';
        this.loading = false;
      }
    });
  }

  private loadTeamTasks(teamId: number): void {
    this.#tasks.list().subscribe({
      next: (rows) => {
        this.teamTasks = (rows ?? []).filter(t => Number(t.teamId ?? 0) === teamId);
      },
      error: () => {
        this.teamTasks = [];
      }
    });
  }
}
