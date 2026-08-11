import { CommonModule, DatePipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, finalize } from 'rxjs';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { HasRoleDirective } from '../directives/has-role.directive';
import { AuthService } from '../services/auth.service';
import { TeamService } from '../services/team.service';
import { UserService } from '../services/user.service';
import type { TeamDetailResponse, TeamMemberRow } from '../models/team-detail.model';
import type { TeamResponse } from '../models/team.model';
import type { UserDirectoryItem } from '../models/user.model';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, HasRoleDirective, DatePipe],
  template: `
    <app-navbar></app-navbar>
    <section class="page">
      <div class="page__head">
        <div>
          <h2 class="title">My Teams</h2>
          <p class="muted">Admin sees all teams. Manager sees teams they own.</p>
        </div>
        <button *appHasRole="['ADMIN','MANAGER']" type="button" class="btn primary" (click)="openCreate()">
          + Create Team
        </button>
      </div>

      <div class="stats">
        <div class="stat stat--green">
          <div class="stat__label">My Teams</div>
          <div class="stat__value">{{ teams.length }}</div>
        </div>
        <div class="stat stat--blue">
          <div class="stat__label">Total Members</div>
          <div class="stat__value">{{ totalMembers }}</div>
        </div>
        <div class="stat stat--amber">
          <div class="stat__label">Active Tasks</div>
          <div class="stat__value">{{ activeTasks }}</div>
        </div>
      </div>

      <div class="card error" *ngIf="error">{{ error }}</div>
      <div class="card" *ngIf="loadingTeams">
        Loading teams...
        <button type="button" class="btn ghost small" (click)="loadTeams()">Retry</button>
      </div>

      <div class="grid" *ngIf="!loadingTeams && !error">
        <article class="team" *ngFor="let t of teams" [class.is-selected]="t.id===selectedTeamId">
          <div class="team__strip" [class]="stripClass(t.id)"></div>
          <div class="team__body">
            <div class="team__name">{{ t.name }}</div>
            <div class="team__meta">{{ t.memberCount }} members</div>
            <div class="team__meta">Manager: {{ t.managerName }}</div>

            <div class="team__chips">
              <span class="avatar" *ngFor="let i of (t.memberInitials || [])">{{ i }}</span>
              <span class="chip">{{ t.activeTasksCount }} tasks active</span>
            </div>

            <div class="team__actions">
              <button type="button" class="btn view" (click)="viewTeam(t.id)">View Team</button>
              <button *appHasRole="['ADMIN','MANAGER']" type="button" class="btn ghost" (click)="editTeam(t.id)">Edit</button>
              <button
                *appHasRole="['ADMIN','MANAGER']"
                type="button"
                class="btn danger"
                (click)="deleteTeam(t)"
              >
                Delete
              </button>
            </div>
          </div>
        </article>

        <div class="card" *ngIf="teams.length === 0">No teams found.</div>
      </div>

      <section class="detail" *ngIf="selectedTeamId" #detailSection>
        <div class="detail__head">
          <div class="detail__title">
            Team Detail - {{ selectedTeam?.name || '-' }}
          </div>
          <div class="detail__actions" *appHasRole="['ADMIN','MANAGER']">
            <input
              class="input"
              type="text"
              placeholder="Search users..."
              [(ngModel)]="addMemberQuery"
              (focus)="ensureUsersLoaded()"
            />
            <select class="select" [(ngModel)]="addMemberUserId" (focus)="ensureUsersLoaded()">
              <option [ngValue]="0">Select member</option>
              <option
                *ngFor="let u of filteredAddableUsers"
                [ngValue]="u.id"
              >
                {{ u.fullName }}{{ u.email ? ' ('+u.email+')' : '' }}
              </option>
            </select>
            <button type="button" class="btn primary" (click)="addMember()">+ Add Member</button>
          </div>
        </div>

        <div class="card" *ngIf="loadingDetail">
          Loading team...
          <button type="button" class="btn ghost small" (click)="loadTeamDetail(selectedTeamId!)">Retry</button>
        </div>

        <div class="table-wrap" *ngIf="!loadingDetail && selectedTeam">
          <table class="table">
            <thead>
              <tr>
                <th>Member</th>
                <th>Role</th>
                <th>Email</th>
                <th>Tasks Assigned</th>
                <th>Joined</th>
                <th class="th-right"></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let m of selectedTeam.members">
                <td class="member">
                  <div class="member__name">{{ m.fullName }}</div>
                </td>
                <td><span class="badge" [class]="roleClass(m.role)">{{ m.role }}</span></td>
                <td class="muted">{{ m.email }}</td>
                <td>{{ m.tasksAssigned }}</td>
                <td>{{ m.joinedAt | date: 'MMM dd' }}</td>
                <td class="th-right" *appHasRole="['ADMIN','MANAGER']">
                  <button
                    type="button"
                    class="btn danger small"
                    [disabled]="m.userId === selectedTeam.managerId"
                    (click)="removeMember(m)"
                  >
                    Remove
                  </button>
                </td>
              </tr>
              <tr *ngIf="selectedTeam.members.length === 0">
                <td colspan="6" class="muted">No members.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Create Team Modal -->
      <div class="modal-backdrop" *ngIf="createOpen" (click)="closeCreate()"></div>
      <div class="modal" *ngIf="createOpen">
        <div class="modal__card" (click)="$event.stopPropagation()">
          <div class="modal__head">
            <div class="modal__title">Create Team</div>
            <button type="button" class="icon-btn" (click)="closeCreate()">&times;</button>
          </div>

          <div class="form">
            <label class="label">Team Name</label>
            <input class="input" type="text" [(ngModel)]="createName" />

            <label class="label">Description</label>
            <textarea class="textarea" rows="3" [(ngModel)]="createDescription"></textarea>

            <ng-container *ngIf="isAdmin">
              <label class="label">Manager</label>
              <select class="select" [(ngModel)]="createManagerId" (focus)="ensureUsersLoaded()">
                <option [ngValue]="0">Me (default)</option>
                <option *ngFor="let u of users" [ngValue]="u.id">{{ u.fullName }}{{ u.email ? ' ('+u.email+')' : '' }}</option>
              </select>
            </ng-container>

            <label class="label">Add Members</label>
            <input
              class="input"
              type="text"
              placeholder="Search..."
              [(ngModel)]="createMemberQuery"
              (focus)="ensureUsersLoaded()"
            />
            <div class="picker">
              <label class="pick" *ngFor="let u of filteredPickUsers">
                <input type="checkbox" [checked]="createMemberIds.has(u.id)" (change)="togglePick(u.id)" />
                <span>{{ u.fullName }}</span>
                <span class="muted" *ngIf="u.email">({{ u.email }})</span>
              </label>
            </div>
          </div>

          <div class="modal__actions">
            <button type="button" class="btn ghost" (click)="closeCreate()">Cancel</button>
            <button type="button" class="btn primary" [disabled]="!createName.trim()" (click)="createTeam()">
              Create
            </button>
          </div>
        </div>
      </div>

      <!-- Edit Team Modal -->
      <div class="modal-backdrop" *ngIf="editOpen" (click)="closeEdit()"></div>
      <div class="modal" *ngIf="editOpen">
        <div class="modal__card" (click)="$event.stopPropagation()">
          <div class="modal__head">
            <div class="modal__title">Edit Team</div>
            <button type="button" class="icon-btn" (click)="closeEdit()">&times;</button>
          </div>

          <div class="form">
            <label class="label">Team Name</label>
            <input class="input" type="text" [(ngModel)]="editName" />

            <label class="label">Description</label>
            <textarea class="textarea" rows="3" [(ngModel)]="editDescription"></textarea>

            <ng-container *ngIf="isAdmin">
              <label class="label">Manager</label>
              <select class="select" [(ngModel)]="editManagerId" (focus)="ensureUsersLoaded()">
                <option *ngFor="let u of users" [ngValue]="u.id">{{ u.fullName }}{{ u.email ? ' ('+u.email+')' : '' }}</option>
              </select>
            </ng-container>
          </div>

          <div class="modal__actions">
            <button type="button" class="btn ghost" (click)="closeEdit()">Cancel</button>
            <button type="button" class="btn primary" [disabled]="!editName.trim()" (click)="saveEdit()">
              Save
            </button>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .page{padding:18px 22px}
      .page__head{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;margin-bottom:14px}
      .title{margin:0}
      .muted{opacity:.75;margin:6px 0 0}

      .stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin:12px 0 16px}
      .stat{padding:12px 14px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.03);position:relative;overflow:hidden}
      .stat:before{content:'';position:absolute;left:0;top:0;bottom:0;width:4px}
      .stat--green:before{background:#10b981}
      .stat--blue:before{background:#3b82f6}
      .stat--amber:before{background:#f59e0b}
      .stat__label{font-size:12px;opacity:.8}
      .stat__value{font-size:28px;font-weight:800;margin-top:2px}

      .grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}
      .team{border-radius:14px;border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.02);overflow:hidden}
      .team.is-selected{outline:2px solid rgba(59,130,246,.45)}
      .team__strip{height:6px}
      .strip--green{background:#10b981}
      .strip--blue{background:#3b82f6}
      .strip--purple{background:#8b5cf6}
      .strip--amber{background:#f59e0b}
      .team__body{padding:12px 14px}
      .team__name{font-weight:800;font-size:16px}
      .team__meta{font-size:13px;opacity:.8;margin-top:2px}
      .team__chips{display:flex;align-items:center;gap:8px;margin:10px 0 12px;flex-wrap:wrap}
      .avatar{width:28px;height:28px;border-radius:999px;border:1px solid rgba(59,130,246,.35);display:inline-flex;align-items:center;justify-content:center;font-weight:800;font-size:11px;background:rgba(59,130,246,.12)}
      .chip{font-size:12px;padding:6px 10px;border-radius:999px;border:1px solid rgba(245,158,11,.35);background:rgba(245,158,11,.10);font-weight:700}
      .team__actions{display:flex;gap:10px}

      .btn{border:1px solid rgba(255,255,255,.14);background:transparent;color:#e2e8f0;border-radius:10px;padding:8px 12px;font-weight:800;cursor:pointer}
      .btn:hover{background:rgba(255,255,255,.06)}
      .btn.primary{background:#0f766e;border-color:#0f766e;color:#fff}
      .btn.primary:hover{filter:brightness(1.05)}
      .btn.view{background:#2563eb;border-color:#2563eb;color:#fff}
      .btn.ghost{background:rgba(255,255,255,.06)}
      .btn.danger{background:#ef4444;border-color:#ef4444;color:#fff}
      .btn.small{padding:6px 10px;border-radius:8px;font-weight:800}
      .btn[disabled]{opacity:.55;cursor:not-allowed}

      .card{padding:12px 14px;border:1px solid rgba(255,255,255,.10);border-radius:12px;background:rgba(255,255,255,.02);margin:10px 0}
      .card.error{border-color:rgba(239,68,68,.35)}

      .detail{margin-top:16px;border:1px solid rgba(255,255,255,.10);border-radius:14px;background:rgba(255,255,255,.02);padding:12px 14px}
      .detail__head{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:10px}
      .detail__title{font-weight:900}
      .detail__actions{display:flex;gap:10px;align-items:center;flex-wrap:wrap}

      .input,.select,.textarea{border:1px solid rgba(148,163,184,.4);background:rgba(15,23,42,.72);color:#f8fafc;border-radius:10px;padding:8px 10px;min-width:220px}
      .modal .input,.modal .select,.modal .textarea{border:1px solid rgba(120,113,108,.65);background:rgba(41,37,36,.92);color:#f5f5f4}
      .modal .input:focus,.modal .select:focus,.modal .textarea:focus{outline:none;border-color:#34d399;box-shadow:0 0 0 3px rgba(16,185,129,.25)}
      .input::placeholder,.textarea::placeholder{color:#cbd5e1;opacity:1}
      .modal .input::placeholder,.modal .textarea::placeholder{color:#d6d3d1}
      .textarea{min-width:100%}
      .select{min-width:240px}
      .label{display:block;font-weight:800;font-size:12px;color:#dbeafe;opacity:1;margin:10px 0 6px}
      .modal .label{color:#e7e5e4}

      .table-wrap{overflow:auto}
      .table{width:100%;border-collapse:collapse}
      th,td{padding:10px;border-bottom:1px solid rgba(255,255,255,.08);text-align:left;font-size:13px}
      th{opacity:.8;font-weight:900}
      .th-right{text-align:right}
      .member__name{font-weight:800}
      .badge{display:inline-flex;align-items:center;padding:4px 10px;border-radius:999px;border:1px solid rgba(255,255,255,.14);font-weight:900;font-size:11px}
      .role--ADMIN{background:rgba(239,68,68,.15);border-color:rgba(239,68,68,.35)}
      .role--MANAGER{background:rgba(139,92,246,.15);border-color:rgba(139,92,246,.35)}
      .role--MEMBER{background:rgba(59,130,246,.15);border-color:rgba(59,130,246,.35)}
      .role--VIEWER{background:rgba(148,163,184,.10);border-color:rgba(148,163,184,.35)}

      .modal-backdrop{position:fixed;inset:0;background:rgba(0,0,0,.55);z-index:9998}
      .modal{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;z-index:9999;padding:16px}
      .modal__card{width:min(720px, 100%);background:#1c1917;border:1px solid rgba(120,113,108,.55);border-radius:16px;overflow:hidden;box-shadow:0 20px 45px rgba(0,0,0,.5)}
      .modal__head{display:flex;align-items:center;justify-content:space-between;padding:12px 14px;border-bottom:1px solid rgba(255,255,255,.10)}
      .modal__title{font-weight:900}
      .icon-btn{border:0;background:transparent;color:#fff;font-size:22px;cursor:pointer;opacity:.85}
      .icon-btn:hover{opacity:1}
      .form{padding:12px 14px}
      .picker{max-height:220px;overflow:auto;border:1px solid rgba(120,113,108,.45);border-radius:12px;padding:8px;margin-top:8px;background:rgba(28,25,23,.7)}
      .pick{display:flex;align-items:center;gap:10px;padding:6px 8px;border-radius:10px}
      .pick:hover{background:rgba(120,113,108,.2)}
      .modal input[type='checkbox']{accent-color:#34d399}
      .modal__actions{display:flex;justify-content:flex-end;gap:10px;padding:12px 14px;border-top:1px solid rgba(255,255,255,.10)}

      @media (max-width: 1100px){
        .grid{grid-template-columns:repeat(2,minmax(0,1fr))}
      }
      @media (max-width: 720px){
        .stats{grid-template-columns:1fr}
        .grid{grid-template-columns:1fr}
        .input,.select{min-width:100%}
      }
    `
  ]
})
export class TeamsComponent implements OnInit, OnDestroy {
  #teams = inject(TeamService);
  #users = inject(UserService);
  #auth = inject(AuthService);
  @ViewChild('detailSection') detailSection?: ElementRef<HTMLElement>;
  private detailReq?: Subscription;
  private teamsReq?: Subscription;

  teams: TeamResponse[] = [];
  selectedTeamId: number | null = null;
  selectedTeam: TeamDetailResponse | null = null;

  loadingTeams = false;
  loadingDetail = false;
  error = '';

  users: UserDirectoryItem[] = [];
  usersLoaded = false;

  addMemberQuery = '';
  addMemberUserId = 0;

  createOpen = false;
  createName = '';
  createDescription = '';
  createManagerId = 0;
  createMemberQuery = '';
  createMemberIds = new Set<number>();

  editOpen = false;
  editTeamId = 0;
  editName = '';
  editDescription = '';
  editManagerId = 0;

  get isAdmin(): boolean {
    return this.#auth.getCurrentUser()?.role === 'ADMIN';
  }

  get totalMembers(): number {
    return this.teams.reduce((sum, t) => sum + (t.memberCount ?? 0), 0);
  }

  get activeTasks(): number {
    return this.teams.reduce((sum, t) => sum + (t.activeTasksCount ?? 0), 0);
  }

  get filteredAddableUsers(): UserDirectoryItem[] {
    const q = this.addMemberQuery.trim().toLowerCase();
    const existing = new Set((this.selectedTeam?.members ?? []).map(m => m.userId));

    return this.users
      .filter(u => !existing.has(u.id))
      .filter(u => (q ? `${u.fullName} ${u.email ?? ''}`.toLowerCase().includes(q) : true))
      .slice(0, 20);
  }

  get filteredPickUsers(): UserDirectoryItem[] {
    const q = this.createMemberQuery.trim().toLowerCase();
    return this.users
      .filter(u => (q ? `${u.fullName} ${u.email ?? ''}`.toLowerCase().includes(q) : true))
      .slice(0, 50);
  }

  ngOnInit(): void {
    this.loadTeams();
  }

  ngOnDestroy(): void {
    this.teamsReq?.unsubscribe();
    this.detailReq?.unsubscribe();
  }

  loadTeams(): void {
    this.loadingTeams = true;
    this.error = '';
    this.teamsReq?.unsubscribe();
    this.teamsReq = this.#teams.list()
      .pipe(finalize(() => { this.loadingTeams = false; }))
      .subscribe({
        next: (res) => {
          this.teams = res ?? [];
          if (this.teams.length === 0) {
            this.selectedTeamId = null;
            this.selectedTeam = null;
            return;
          }

          const hasCurrent = this.selectedTeamId
            ? this.teams.some(t => t.id === this.selectedTeamId)
            : false;

          if (hasCurrent && this.selectedTeamId) {
            this.loadTeamDetail(this.selectedTeamId);
          } else {
            const firstTeamId = this.teams[0].id;
            this.selectedTeamId = firstTeamId;
            this.loadTeamDetail(firstTeamId);
          }
        },
        error: (err) => {
          this.error = err?.error?.message || err?.message || 'Failed to load teams. Please try again.';
          this.teams = [];
          this.selectedTeam = null;
          this.selectedTeamId = null;
        }
      });
  }

  selectTeam(teamId: number): void {
    if (!teamId) return;
    this.openTeamDetail(teamId, false);
  }

  viewTeam(teamId: number): void {
    this.openTeamDetail(teamId, false);
    setTimeout(() => this.scrollToDetail(), 0);
  }

  editTeam(teamId: number): void {
    const target = this.teams.find(t => t.id === teamId);
    if (!target) return;

    this.editTeamId = teamId;
    this.editName = target.name;
    this.editDescription = target.description ?? '';
    this.editManagerId = target.managerId ?? 0;
    this.editOpen = true;
    this.ensureUsersLoaded();
  }

  loadTeamDetail(teamId: number): void {
    this.loadingDetail = true;
    this.detailReq?.unsubscribe();
    this.detailReq = this.#teams.get(teamId)
      .pipe(finalize(() => { this.loadingDetail = false; }))
      .subscribe({
        next: (res) => {
          this.selectedTeam = res;
        },
        error: (err) => {
          this.error = err?.error?.message || err?.message || 'Failed to load team. Please try again.';
          this.selectedTeam = null;
        }
      });
  }

  ensureUsersLoaded(): void {
    if (this.usersLoaded) return;
    this.#users.list().subscribe({
      next: (res) => {
        this.users = res ?? [];
        this.usersLoaded = true;
      },
      error: () => {
        // ignore; UI will work without picker
      }
    });
  }

  addMember(): void {
    const teamId = this.selectedTeamId ?? 0;
    const userId = this.addMemberUserId ?? 0;
    if (!teamId || !userId) return;
    this.#teams.addMember(teamId, userId).subscribe({
      next: () => {
        this.addMemberUserId = 0;
        this.addMemberQuery = '';
        this.loadTeamDetail(teamId);
        this.loadTeams();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to add member.';
      }
    });
  }

  removeMember(m: TeamMemberRow): void {
    const teamId = this.selectedTeamId ?? 0;
    if (!teamId) return;
    if (!confirm(`Remove ${m.fullName} from this team?`)) return;
    this.#teams.removeMember(teamId, m.userId).subscribe({
      next: () => {
        this.loadTeamDetail(teamId);
        this.loadTeams();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to remove member.';
      }
    });
  }

  deleteTeam(t: TeamResponse): void {
    if (!confirm(`Delete team "${t.name}"?`)) return;
    this.#teams.delete(t.id).subscribe({
      next: () => {
        if (this.selectedTeamId === t.id) {
          this.selectedTeamId = null;
          this.selectedTeam = null;
        }
        this.loadTeams();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to delete team.';
      }
    });
  }

  openCreate(): void {
    this.createOpen = true;
    this.createName = '';
    this.createDescription = '';
    this.createManagerId = 0;
    this.createMemberQuery = '';
    this.createMemberIds.clear();
    this.ensureUsersLoaded();
  }

  closeCreate(): void {
    this.createOpen = false;
  }

  closeEdit(): void {
    this.editOpen = false;
    this.editTeamId = 0;
    this.editName = '';
    this.editDescription = '';
    this.editManagerId = 0;
  }

  togglePick(userId: number): void {
    if (this.createMemberIds.has(userId)) this.createMemberIds.delete(userId);
    else this.createMemberIds.add(userId);
  }

  createTeam(): void {
    const name = this.createName.trim();
    if (!name) return;

    const payload: any = {
      name,
      description: this.createDescription?.trim() || undefined,
      memberIds: Array.from(this.createMemberIds)
    };

    if (this.isAdmin && this.createManagerId) {
      payload.managerId = this.createManagerId;
    }

    this.#teams.create(payload).subscribe({
      next: () => {
        this.closeCreate();
        this.loadTeams();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to create team.';
      }
    });
  }

  saveEdit(): void {
    const teamId = this.editTeamId;
    const name = this.editName.trim();
    if (!teamId || !name) return;

    const payload: any = {
      name,
      description: this.editDescription?.trim() || undefined
    };

    if (this.isAdmin && this.editManagerId) {
      payload.managerId = this.editManagerId;
    }

    this.#teams.update(teamId, payload).subscribe({
      next: () => {
        this.closeEdit();
        this.loadTeams();
        this.openTeamDetail(teamId, true);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update team.';
      }
    });
  }

  roleClass(role: string): string {
    switch ((role ?? '').toUpperCase()) {
      case 'ADMIN':
        return 'role--ADMIN';
      case 'MANAGER':
        return 'role--MANAGER';
      case 'MEMBER':
        return 'role--MEMBER';
      default:
        return 'role--VIEWER';
    }
  }

  stripClass(teamId: number): string {
    const v = Math.abs(teamId) % 4;
    if (v === 0) return 'strip--green';
    if (v === 1) return 'strip--blue';
    if (v === 2) return 'strip--purple';
    return 'strip--amber';
  }

  private scrollToDetail(): void {
    this.detailSection?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  private openTeamDetail(teamId: number, forceReload: boolean): void {
    if (!teamId) return;
    this.error = '';
    if (forceReload) {
      this.selectedTeam = null;
    }
    this.selectedTeamId = teamId;
    this.loadTeamDetail(teamId);
  }
}




