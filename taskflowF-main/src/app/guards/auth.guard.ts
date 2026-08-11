import { Injectable, inject } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  #auth = inject(AuthService);
  #router = inject(Router);
  canActivate(): boolean | UrlTree {
    return this.#auth.isAuthenticated() ? true : this.#router.parseUrl('/login');
  }
}