import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Subscription, filter } from 'rxjs';
import { HasRoleDirective } from '../../directives/has-role.directive';

@Component({
  standalone: true,
  selector: 'app-navbar',
  imports: [RouterLink, NgIf, HasRoleDirective],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  // constructor(public auth: AuthService) {}
 
 fullName?: string;
  private sub?: Subscription;

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.readFromStorage(); // initial read (page refresh)
    // re-read after every navigation end (e.g., after login redirect)
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.readFromStorage());
  }

  private readFromStorage(): void {
    this.fullName = this.auth.getCurrentUser()?.fullName ?? undefined;
  }

  onLogout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.fullName = undefined;
    this.router.navigate(['/login']); // optional
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }



}
