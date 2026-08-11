import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import type { UserRole } from '../models/role.model';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  #auth = inject(AuthService);
  #router = inject(Router);
  #toast = inject(ToastService);

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const allowed = (route.data?.['roles'] as UserRole[] | undefined) ?? [];
    if (allowed.length === 0) return true;

    const user = this.#auth.getCurrentUser();
    const role = user?.role;
    if (role && allowed.includes(role)) return true;

    this.#toast.show('You do not have permission to view this page.', 'error');
    return this.#router.parseUrl('/dashboard');
  }
}

