// path: src/app/services/auth.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { map, catchError } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, RegisterRequest } from '../models/auth.model';
import type { CurrentUser } from '../models/current-user.model';
import type { UserRole } from '../models/role.model';

const TOKEN_KEY = 'taskflow_token';
@Injectable({ providedIn: 'root' })
export class AuthService {
  #http = inject(HttpClient);
  #router = inject(Router);
  #base = `${environment.apiBaseUrl}/api/auth`;

  /** Login - returns AuthResponse from API */
  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.#http.post<AuthResponse>(`${this.#base}/login`, payload);
  }

  /**
   * Register - API often returns 201/204 without body.
   * Expose Observable<void> to keep component simple.
   */
  register(payload: RegisterRequest): Observable<void> {
    return this.#http
      .post<void>(`${this.#base}/register`, payload, { observe: 'response' })
      .pipe(
        map((res: HttpResponse<void>) => {
          // normalize to void regardless of status code
          return;
        })
      );
  }

  /** Token helpers */
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  setLoginResult(res: AuthResponse) {
    // store token + user info together
    this.setToken(res.token);

    const payload = this.decodeJwt(res.token);
    const role = (payload?.role as UserRole | undefined) ?? 'MEMBER';
    const userId = Number(payload?.userId ?? payload?.uid ?? res.userId ?? 0) || 0;
    const fullName = String(payload?.fullName ?? payload?.name ?? res.fullName ?? '');

    const user: CurrentUser = {
      userId,
      email: res.email,
      fullName,
      role
    };
    localStorage.setItem('user', JSON.stringify(user));
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const payload = this.decodeJwt(token);
    if (payload && typeof payload.exp === 'number') {
      return !this.isTokenExpired(payload.exp);
    }
    // If not a JWT or no exp, consider authenticated when token exists
    return true;
  }

  /** Get current user ID from localStorage */
  getCurrentUserId(): number {
    return this.getCurrentUser()?.userId ?? 0;
  }

  getCurrentUser(): CurrentUser | null {
    const raw = localStorage.getItem('user');
    if (raw) {
      try {
        const parsed = JSON.parse(raw);
        if (
          parsed &&
          typeof parsed === 'object' &&
          typeof parsed.userId === 'number' &&
          typeof parsed.fullName === 'string' &&
          typeof parsed.email === 'string' &&
          typeof parsed.role === 'string'
        ) {
          return parsed as CurrentUser;
        }
      } catch {
        // fall through to token-based recovery
      }
    }

    const token = this.getToken();
    if (!token) return null;
    const payload = this.decodeJwt(token);
    if (!payload) return null;

    const role = (payload?.role as UserRole | undefined) ?? 'MEMBER';
    const userId = Number(payload?.userId ?? payload?.uid ?? 0) || 0;
    const fullName = String(payload?.fullName ?? payload?.name ?? '');
    const email = String(payload?.sub ?? '');

    const recovered: CurrentUser = { userId, fullName, email, role };
    localStorage.setItem('user', JSON.stringify(recovered));
    return recovered;
  }

  updateStoredUser(patch: Partial<CurrentUser>): void {
    const current = this.getCurrentUser();
    if (!current) return;
    const next: CurrentUser = { ...current, ...patch };
    localStorage.setItem('user', JSON.stringify(next));
  }

  /** Logout + navigate to /login (idempotent) */
  logout(): void {
    this.clearToken();
    localStorage.removeItem('user');
    // ignore navigation errors (e.g., already on /login)
    this.#router.navigate(['/login']).catch(() => {});
  }

  // ---------------- Private helpers ----------------

  /** Decode JWT payload safely; returns null if not a valid JWT */
  private decodeJwt(token: string): any | null {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    try {
      const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      // decodeURIComponent isn’t needed here; base64url decoded already
      return JSON.parse(payload);
    } catch {
      return null;
    }
  }

  private isTokenExpired(expSeconds: number): boolean {
    const nowSeconds = Math.floor(Date.now() / 1000);
    return expSeconds <= nowSeconds;
  }
}

// @Injectable({ providedIn: 'root' })
// export class AuthService {
//   #http = inject(HttpClient);
//   #router = inject(Router);
//   #base = `${environment.apiBaseUrl}/api/auth`;

  

//   /** Login – returns AuthResponse from API */
//   login(payload: AuthRequest): Observable<AuthResponse> {
//     return this.#http.post<AuthResponse>(`${this.#base}/login`, payload);
//   }

//   /**
//    * Register – API often returns 201/204 without body.
//    * Expose Observable<void> to keep component simple.
//    */
//   register(payload: RegisterRequest): Observable<void> {
//     return this.#http
//       .post<void>(`${this.#base}/register`, payload, { observe: 'response' })
//       .pipe(
//         map((res: HttpResponse<void>) => {
//           // normalize to void regardless of status code
//           return;
//         })
//       );
//   }

//   /** Token helpers */
//   setToken(token: string): void {
//     localStorage.setItem(TOKEN_KEY, token);
//   }

//   getToken(): string | null {
//     return localStorage.getItem(TOKEN_KEY);
//   }

//   clearToken(): void {
//     localStorage.removeItem(TOKEN_KEY);
//   }
  
// setLoginResult(res: { token: string; id: number | string; fullName: string; email: string }) {
//     // persist token + user so refresh keeps you logged in
//     localStorage.setItem('token', res.token);
//     localStorage.setItem('user', JSON.stringify({ id: res.id, email: res.email, fullName: res.fullName }));
    
//   }


//   /**
//    * Auth check:
//    * - If token looks like a JWT and has 'exp', validate expiry.
//    * - Otherwise, fallback to presence check (truthy token).
//    */
//   isAuthenticated(): boolean {
//     const token = this.getToken();
//     if (!token) return false;

//     const payload = this.decodeJwt(token);
//     if (payload && typeof payload.exp === 'number') {
//       return !this.isTokenExpired(payload.exp);
//     }
//     // If not a JWT or no exp, consider authenticated when token exists
//     return true;
//   }

//   /** Logout + navigate to /login (idempotent) */
//   logout(): void {
//     this.clearToken();
//     // ignore navigation errors (e.g., already on /login)
//     this.#router.navigate(['/login']).catch(() => {});
//   }

//   // ----------------- Private helpers -----------------

//   /** Decode JWT payload safely; returns null if not a valid JWT */
//   private decodeJwt(token: string): any | null {
//     const parts = token.split('.');
//     if (parts.length !== 3) return null;
//     try {
//       const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
//       // decodeURIComponent isn’t needed here; base64url decoded already
//       return JSON.parse(payload);
//     } catch {
//       return null;
//     }
//   }

//   private isTokenExpired(expSeconds: number): boolean {
//     const nowSeconds = Math.floor(Date.now() / 1000);
//     return expSeconds <= nowSeconds;
//   }
// }
