import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class GuestGuard implements CanActivate {
  #auth = inject(AuthService);
  #router = inject(Router);
  canActivate(): boolean | UrlTree {
    return this.#auth.isAuthenticated() ? this.#router.parseUrl('/dashboard') : true;
  }
}