import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
  FormGroup,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { AuthService } from '../../core/services/auth.service';
import { AuthRequest } from '../../core/models/auth-request.model';
import { AlertDialog } from '../../shared/alert-dialog/alert-dialog';
import { createDialogForSuccessMessage } from '../../core/models/alert-dialog.factory';

//import { createDialogForApiError, createDialogForSuccessMessage } from '../../core/models/alert-dialog.factory';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.html',
  styleUrls: ['./register.scss'],
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.currentUser;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly message = this.authService.message;
  readonly errorStatus = this.authService.errorStatus;
  readonly errorPath = this.authService.errorPath;
  readonly errorTimestamp = this.authService.errorTimestamp;

  registerForm: FormGroup = this.fb.group(
    {
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    {
      validators: this.passwordMatchValidator,
    }
  );

  loading = false;
  showPassword = false;
  showConfirmPassword = false;

  /** Validator to ensure password and confirmPassword match */
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  get email(): AbstractControl | null {
    return this.registerForm.get('email');
  }

  get password(): AbstractControl | null {
    return this.registerForm.get('password');
  }

  get confirmPassword(): AbstractControl | null {
    return this.registerForm.get('confirmPassword');
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: AuthRequest = {
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
    };

    this.authService
      .register(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loading = false;
          this.dialog.open(AlertDialog, {
            panelClass: 'rounded-dialog',
            data: createDialogForSuccessMessage(this.message() || 'Registration successful'),
          });
          console.log('Register payload:', payload);
          this.authService.message.set(null);
          this.router.navigate(['/login']);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          if (!err?.error) {
            this.dialog.open(AlertDialog, {
              panelClass: 'rounded-dialog',
              data: { title: 'Error', message: 'Registration failed. Please try again.' },
            });
          }
        },
      });
  }
}
export class DialogData {
  data = inject(MAT_DIALOG_DATA);
}
