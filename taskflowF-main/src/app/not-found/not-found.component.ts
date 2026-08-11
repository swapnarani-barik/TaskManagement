// path: src/app/not-found/not-found.component.ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../shared/navbar/navbar.component';

@Component({
  standalone: true,
  selector: 'app-not-found',
  imports: [CommonModule, RouterLink, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <div class="container" style="text-align:center; padding:3rem 1rem;">
      <h1 style="font-size:3rem; margin:0;">404</h1>
      <p class="muted" style="margin:.5rem 0 1.25rem 0;">The page you’re looking for isn’t here.</p>
      <a class="btn primary" routerLink="/dashboard">Go to Dashboard</a>
      <span style="display:inline-block; width:.5rem;"></span>
      <a class="btn ghost" routerLink="/login">Back to Login</a>
    </div>
  `
})
export class NotFoundComponent {}