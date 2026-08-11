import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import {
  FormBuilder,
  Validators,
  ReactiveFormsModule,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';

/** Cross-field validator: flags mismatch ONLY when confirm has a value and differs */
function matchValidator(a: string, b: string) {
  return (ctrl: AbstractControl): ValidationErrors | null => {
    const v1 = ctrl.get(a)?.value ?? '';
    const v2 = ctrl.get(b)?.value ?? '';
    if (v2 && v1 !== v2) return { mismatch: true };
    return null;
  };
}

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, CommonModule, NavbarComponent],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  // DI
  #fb = inject(FormBuilder);
  #auth = inject(AuthService);
  #router = inject(Router);
  #destroyRef = inject(DestroyRef);

  // UI State
  loading = false;
  error: string | null = null;
  success = false;

  /** Meter state: number of bars (0..5) + label */
  strengthLevel = 0;
  strengthLabel: 'Weak' | 'Good' | 'Strong' | '' = '';

  // Reactive Form
  form = this.#fb.group(
    {
      fullName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: matchValidator('password', 'confirmPassword') }
  );

  // -------- Typed Getters for template cleanliness --------
  get fullNameCtrl() { return this.form.controls['fullName']; }
  get emailCtrl() { return this.form.controls['email']; }
  get passwordCtrl() { return this.form.controls['password']; }
  get confirmPasswordCtrl() { return this.form.controls['confirmPassword']; }

  ngOnInit(): void {
    // Live password strength calculation
    this.passwordCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe(v => {
        const val = v ?? '';
        this.updateStrength(val);
        // Re-run match validator because password changed
        this.form.updateValueAndValidity({ onlySelf: false, emitEvent: false });
      });

    // Make mismatch feedback reactive while typing confirm
    this.confirmPasswordCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe(() => {
        this.form.updateValueAndValidity({ onlySelf: false, emitEvent: false });
      });
  }

  /** Compute 0..5 bars + label Weak/Good/Strong */
  private updateStrength(v: string): void {
    let score = 0;
    if (v.length >= 8) score++;
    if (/[A-Z]/.test(v)) score++;
    if (/[a-z]/.test(v)) score++;
    if (/\d/.test(v)) score++;
    if (/[^A-Za-z0-9]/.test(v)) score++;

    // Optional bonus for longer passphrases (keeps bars capped at 5)
    if (v.length >= 12 && score < 5) score++;

    this.strengthLevel = Math.min(score, 5);

    if (!v) this.strengthLabel = '';
    else if (this.strengthLevel >= 5 && v.length > 12) this.strengthLabel = 'Strong';
    else if (this.strengthLevel >= 4) this.strengthLabel = 'Good';
    else this.strengthLabel = 'Weak';
  }

  /** Submit handler */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = null;
    this.success = false;

    const { fullName, email, password } = this.form.getRawValue() as {
      fullName: string; email: string; password: string;
    };

    this.#auth.register({ fullName, email, password })
      .pipe(takeUntilDestroyed(this.#destroyRef))
      .subscribe({
        next: () => {
          this.success = true;
          this.loading = false;
          setTimeout(() => this.#router.navigate(['/login']), 800);
        },
        error: (err) => {
          this.error = err?.error?.message ?? 'Registration failed.';
          this.loading = false;
        }
      });
  }
}