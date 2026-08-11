import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { AdminService } from '../services/admin.service';
import { ToastService } from '../services/toast.service';
import type { AdminRole, AdminUser } from '../models/admin-user.model';

type RoleFilter = 'ALL' | AdminRole | 'INACTIVE';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  template: `
    <app-navbar></app-navbar>

    <section class="page">
      <div class="page__head">
        <div>
          <h2 class="title">Admin Panel — User Management</h2>
          <p class="muted">Full system control. Only Admins can access this page.</p>
        </div>
      </div>

      <div class="stats">
        <div class="stat stat--blue">
          <div class="stat__label">Total Users</div>
          <div class="stat__value">{{ users.length }}</div>
        </div>
        <div class="stat stat--red">
          <div class="stat__label">Admins</div>
          <div class="stat__value">{{ countByRole('ADMIN') }}</div>
        </div>
        <div class="stat stat--purple">
          <div class="stat__label">Managers</div>
          <div class="stat__value">{{ countByRole('MANAGER') }}</div>
        </div>
        <div class="stat stat--grey">
          <div class="stat__label">Deactivated</div>
          <div class="stat__value">{{ users.filter(u => !u.active).length }}</div>
        </div>
      </div>

      <div class="card error" *ngIf="error">{{ error }}</div>
      <div class="card" *ngIf="loading">Loading…</div>

      <div class="filters" *ngIf="!loading && !error">
        <input class="input" type="text" placeholder="Search users…" [(ngModel)]="query" />
        <div class="tabs">
          <button type="button" class="tab" [class.is-active]="roleFilter==='ALL'" (click)="roleFilter='ALL'">
            All
          </button>
          <button type="button" class="tab" [class.is-active]="roleFilter==='ADMIN'" (click)="roleFilter='ADMIN'">
            Admin
          </button>
          <button type="button" class="tab" [class.is-active]="roleFilter==='MANAGER'" (click)="roleFilter='MANAGER'">
            Manager
          </button>
          <button type="button" class="tab" [class.is-active]="roleFilter==='MEMBER'" (click)="roleFilter='MEMBER'">
            Member
          </button>
          <button type="button" class="tab" [class.is-active]="roleFilter==='VIEWER'" (click)="roleFilter='VIEWER'">
            Viewer
          </button>
          <button type="button" class="tab" [class.is-active]="roleFilter==='INACTIVE'" (click)="roleFilter='INACTIVE'">
            Inactive
          </button>
        </div>
      </div>

      <table class="table" *ngIf="!loading && !error">
        <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Teams</th>
            <th class="th-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let u of filteredUsers()">
            <td class="user">
              <span class="avatar">{{ initials(u.fullName) }}</span>
              <span class="user__name">{{ u.fullName }}</span>
            </td>
            <td>{{ u.email }}</td>
            <td>
              <select class="select" [ngModel]="u.role" (ngModelChange)="onRoleChange(u, $event)">
                <option value="ADMIN">ADMIN</option>
                <option value="MANAGER">MANAGER</option>
                <option value="MEMBER">MEMBER</option>
                <option value="VIEWER">VIEWER</option>
              </select>
            </td>
            <td>
              <span class="status" [class.status--active]="u.active" [class.status--inactive]="!u.active">
                {{ u.active ? 'Active' : 'Inactive' }}
              </span>
            </td>
            <td class="teams">
              <span *ngIf="(u.teams.length || 0) > 0; else noTeams">
                {{ teamNames(u) }}
              </span>
              <ng-template #noTeams><span class="muted">—</span></ng-template>
            </td>
            <td class="th-right">
              <button
                type="button"
                class="btn warn"
                *ngIf="u.active"
                [disabled]="busyUserId===u.id"
                (click)="toggleStatus(u, false)"
              >
                Deactivate
              </button>
              <button
                type="button"
                class="btn success"
                *ngIf="!u.active"
                [disabled]="busyUserId===u.id"
                (click)="toggleStatus(u, true)"
              >
                Activate
              </button>
              <button
                type="button"
                class="btn danger"
                [disabled]="busyUserId===u.id"
                (click)="deleteUser(u)"
              >
                Delete
              </button>
            </td>
          </tr>
          <tr *ngIf="filteredUsers().length === 0">
            <td colspan="6" class="muted">No users match your filters.</td>
          </tr>
        </tbody>
      </table>

      <div class="rbac">
        <div class="rbac__title">Role Permissions Quick Reference:</div>
        <div class="rbac__text">
          Admin: full control | Manager: create teams, assign tasks | Member: create & edit own tasks | Viewer: read-only.
        </div>
        <div class="rbac__note">Role changes take effect on the user’s next login (new JWT issued).</div>
      </div>
    </section>
  `,
  styles: [
    `
      .page{padding:18px 22px}
      .title{margin:0}
      .muted{opacity:.75;margin:6px 0 0}

      .stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin:12px 0 16px}
      .stat{padding:12px 14px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(255,255,255,.03);position:relative;overflow:hidden}
      .stat:before{content:'';position:absolute;left:0;top:0;bottom:0;width:4px}
      .stat--blue:before{background:#3b82f6}
      .stat--red:before{background:#ef4444}
      .stat--purple:before{background:#8b5cf6}
      .stat--grey:before{background:#94a3b8}
      .stat__label{font-size:12px;opacity:.8}
      .stat__value{font-size:28px;font-weight:900;margin-top:2px}

      .card{padding:12px 14px;border:1px solid rgba(255,255,255,.10);border-radius:12px;background:rgba(255,255,255,.02);margin:10px 0}
      .card.error{border-color:rgba(239,68,68,.35)}

      .filters{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding:10px 12px;border:1px solid rgba(255,255,255,.10);border-radius:12px;background:rgba(255,255,255,.02);margin:10px 0 12px}
      .input{min-width:260px;border:1px solid rgba(255,255,255,.14);background:rgba(0,0,0,.12);color:#e2e8f0;border-radius:10px;padding:8px 10px}
      .tabs{display:flex;gap:8px;flex-wrap:wrap}
      .tab{border:1px solid rgba(255,255,255,.12);background:transparent;color:#e2e8f0;border-radius:10px;padding:7px 12px;font-weight:900;cursor:pointer}
      .tab.is-active{background:#2563eb;border-color:#2563eb;color:#fff}

      .table{width:100%;border-collapse:collapse}
      th,td{padding:10px;border-bottom:1px solid rgba(255,255,255,.08);text-align:left;vertical-align:middle}
      th{opacity:.85;font-weight:900;font-size:12px}
      .th-right{text-align:right}

      .user{display:flex;align-items:center;gap:10px}
      .avatar{width:30px;height:30px;border-radius:999px;display:inline-flex;align-items:center;justify-content:center;font-weight:900;font-size:11px;border:1px solid rgba(59,130,246,.35);background:rgba(59,130,246,.12)}
      .user__name{font-weight:900}

      .select{border:1px solid #2563eb;background:#2563eb;color:#fff;border-radius:10px;padding:7px 10px;font-weight:900}
      .select:focus{outline:none;box-shadow:0 0 0 3px rgba(37,99,235,.35)}
      .select option{background:#0b1224;color:#fff}

      .status{display:inline-flex;align-items:center;justify-content:center;padding:4px 12px;border-radius:999px;border:1px solid rgba(255,255,255,.14);font-weight:900;font-size:12px}
      .status--active{border-color:rgba(34,197,94,.35);background:rgba(34,197,94,.12)}
      .status--inactive{border-color:rgba(239,68,68,.35);background:rgba(239,68,68,.12)}

      .teams{max-width:320px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}

      .btn{border:1px solid rgba(255,255,255,.14);background:transparent;color:#e2e8f0;border-radius:10px;padding:7px 12px;font-weight:900;cursor:pointer;margin-left:8px}
      .btn:first-child{margin-left:0}
      .btn.warn{background:#f59e0b;border-color:#f59e0b;color:#111827}
      .btn.success{background:#22c55e;border-color:#22c55e;color:#111827}
      .btn.danger{background:#ef4444;border-color:#ef4444;color:#fff}
      .btn[disabled]{opacity:.55;cursor:not-allowed}

      .rbac{margin-top:14px;padding:12px 14px;border-radius:12px;border:1px solid rgba(59,130,246,.35);background:rgba(59,130,246,.08)}
      .rbac__title{font-weight:900;margin-bottom:4px}
      .rbac__text{opacity:.9;font-size:13px}
      .rbac__note{opacity:.75;font-size:12px;margin-top:4px}

      @media (max-width: 980px){
        .stats{grid-template-columns:repeat(2,minmax(0,1fr))}
        .teams{max-width:180px}
      }
      @media (max-width: 720px){
        .stats{grid-template-columns:1fr}
        .input{min-width:100%}
      }
    `
  ]
})
export class AdminComponent implements OnInit {
  #admin = inject(AdminService);
  #toast = inject(ToastService);

  users: AdminUser[] = [];
  loading = false;
  error = '';
  query = '';
  roleFilter: RoleFilter = 'ALL';
  busyUserId: number | null = null;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.#admin.listUsers().subscribe({
      next: (res) => {
        this.users = (res ?? []).map((u: any) => ({
          id: Number(u.id ?? 0) || 0,
          fullName: String(u.fullName ?? ''),
          email: String(u.email ?? ''),
          role: (String(u.role ?? 'MEMBER').toUpperCase() as AdminRole) || 'MEMBER',
          active: Boolean(u.active ?? u.isActive ?? true),
          joinedAt: String(u.joinedAt ?? ''),
          teams: Array.isArray(u.teams) ? u.teams : []
        }));
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load users.';
        this.loading = false;
      }
    });
  }

  countByRole(role: AdminRole): number {
    return this.users.filter(u => u.role === role).length;
  }

  filteredUsers(): AdminUser[] {
    const q = this.query.trim().toLowerCase();
    let items = this.users.slice();

    if (this.roleFilter === 'INACTIVE') {
      items = items.filter(u => !u.active);
    } else if (this.roleFilter !== 'ALL') {
      items = items.filter(u => u.role === this.roleFilter);
    }

    if (q) {
      items = items.filter(u => {
        const hay = `${u.fullName} ${u.email} ${(u.teams ?? []).map(t => t.name).join(' ')}`.toLowerCase();
        return hay.includes(q);
      });
    }

    return items;
  }

  initials(fullName: string): string {
    const s = (fullName ?? '').trim();
    if (!s) return 'U';
    const parts = s.split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  teamNames(u: AdminUser): string {
    return (u.teams ?? []).map(t => t.name).join(', ');
  }

  onRoleChange(u: AdminUser, next: AdminRole): void {
    const role = (String(next).toUpperCase() as AdminRole) || u.role;
    if (role === u.role) return;

    this.busyUserId = u.id;
    this.#admin.changeRole(u.id, role).subscribe({
      next: () => {
        u.role = role;
        this.busyUserId = null;
        this.#toast.show('Role updated.', 'success');
      },
      error: (err) => {
        this.busyUserId = null;
        this.#toast.show(err?.error?.message || 'Failed to update role.', 'error');
      }
    });
  }

  toggleStatus(u: AdminUser, active: boolean): void {
    if (u.active === active) return;
    this.busyUserId = u.id;
    this.#admin.changeStatus(u.id, active).subscribe({
      next: () => {
        u.active = active;
        this.busyUserId = null;
        this.#toast.show(active ? 'User activated.' : 'User deactivated.', 'success');
      },
      error: (err) => {
        this.busyUserId = null;
        this.#toast.show(err?.error?.message || 'Failed to update status.', 'error');
      }
    });
  }

  deleteUser(u: AdminUser): void {
    if (!confirm(`Delete user "${u.fullName}"? This cannot be undone.`)) return;
    this.busyUserId = u.id;
    this.#admin.deleteUser(u.id).subscribe({
      next: () => {
        this.users = this.users.filter(x => x.id !== u.id);
        this.busyUserId = null;
        this.#toast.show('User deleted.', 'success');
      },
      error: (err) => {
        this.busyUserId = null;
        this.#toast.show(err?.error?.message || 'Failed to delete user.', 'error');
      }
    });
  }
}
