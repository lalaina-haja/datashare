// src/app/features/auth/components/register/register.ts

// Angular core modules
import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
  FormGroup,
} from "@angular/forms";
import { Router } from "@angular/router";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { HttpErrorResponse } from "@angular/common/http";

// Angular Material modules
import { MatCardModule } from "@angular/material/card";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatDialog } from "@angular/material/dialog";

// Services and Models
import { AuthService } from "../../services/auth.service";
import { AuthRequest } from "../../models/auth.request.model";
import { AlertDialog } from "../../../../shared/dialog/components/alert-dialog/alert-dialog";
import { createDialogForSuccessMessage } from "../../../../shared/dialog/models/alert-dialog.factory";

/** Register Component */
@Component({
  selector: "app-register",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./register.html",
  styleUrls: ["./register.scss"],
})
export class Register implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly message = this.authService.message;
  readonly error = this.authService.errorStatus;

  registerForm: FormGroup = this.fb.group(
    {
      email: ["", [Validators.required, Validators.email]],
      password: ["", [Validators.required, Validators.minLength(8)]],
      confirmPassword: ["", Validators.required],
    },
    {
      validators: this.passwordMatchValidator,
    },
  );

  loading = false;
  showPassword = false;
  showConfirmPassword = false;

  /** Validator to ensure password and confirmPassword match */
  private passwordMatchValidator(
    control: AbstractControl,
  ): ValidationErrors | null {
    const password = control.get("password")?.value;
    const confirmPassword = control.get("confirmPassword")?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  get email(): AbstractControl | null {
    return this.registerForm.get("email");
  }

  get password(): AbstractControl | null {
    return this.registerForm.get("password");
  }

  get confirmPassword(): AbstractControl | null {
    return this.registerForm.get("confirmPassword");
  }

  ngOnInit() {
    this.authService.checkAuthStatus().subscribe();
    if (this.authService.isAuthenticated()) {
      console.log("Already authenticated");
      this.message.set("Connected as " + this.currentUser()?.email);
    }
  }

  login(): void {
    this.authService.clearMessage();
    this.router.navigate(["/login"]);
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
            panelClass: "rounded-dialog",
            data: createDialogForSuccessMessage(
              this.message() || "Registration successful",
            ),
          });
          console.log("Register payload:", payload);
          this.authService.clearMessage();
          this.router.navigate(["/login"]);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          console.error("Registration error:", err);
          if (!err?.error?.message) {
            this.dialog.open(AlertDialog, {
              panelClass: "rounded-dialog",
              data: {
                title: "Error",
                message: "Registration failed. Please try again.",
              },
            });
          }
        },
      });
  }
}
