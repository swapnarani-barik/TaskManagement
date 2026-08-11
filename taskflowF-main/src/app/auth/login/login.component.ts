// import { Component, inject } from '@angular/core';
// import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
// import { Router, RouterLink } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { AuthService } from '../../services/auth.service';
// import { NavbarComponent } from '../../shared/navbar/navbar.component';

// @Component({
//   standalone: true,
//   selector: 'app-login',
//   imports: [ReactiveFormsModule, RouterLink, CommonModule, NavbarComponent],
//   templateUrl: './login.component.html',
//   styleUrls: ['./login.component.css']
// })
// export class LoginComponent {
//   #fb = inject(FormBuilder);
//   #auth = inject(AuthService);
//   #router = inject(Router);

//   loading = false;
//   error: string | null = null;

//   form = this.#fb.group({
//     email: ['', [Validators.required, Validators.email]],
//     password: ['', [Validators.required]]
//   });

//   onSubmit() {
//     if (this.form.invalid) {
//       this.form.markAllAsTouched();   // surface validation immediately
//       return;
//     }
//     this.loading = true;
//     this.error = null;

//     const { email, password } = this.form.getRawValue();

//     this.#auth.login({ email, password } as any).subscribe({
//       next: (res) => {
//         this.#auth.setToken(res.token);
//         this.loading = false;         // clear in success path too
//         this.#router.navigate(['/dashboard']);
        
//       },
//       error: (err) => {
//         this.error = err?.error?.message ?? 'Invalid email or password. Please try again.';
//         this.loading = false;
//       }
//     });
//   }
// }
import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from './../../shared/navbar/navbar.component';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, CommonModule, NavbarComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  #fb = inject(FormBuilder);
  #auth = inject(AuthService);
  #router = inject(Router);

  loading = false;
  error: string | null = null;

  form = this.#fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // surface validation immediately
      return;
    }

    this.loading = true;
    this.error = null;

    const raw = this.form.getRawValue();
    const email: string = raw.email ?? '';
    const password: string = raw.password ?? '';

    this.#auth.login({email, password}).subscribe({
      next: (res) => {
        // save token + user data in one step
        this.#auth.setLoginResult(res);
        this.loading = false; // clear in success path too
        this.#router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Invalid email or password. Please try again.';
        this.loading = false;
      }
    });
  }
}
