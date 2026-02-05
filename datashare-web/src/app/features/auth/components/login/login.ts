// src/app/features/auth/components/login/login.ts

// Angular modules
import { Component, DestroyRef, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup,
  AbstractControl,
} from "@angular/forms";
import { Router } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

// Angular Material modules
import { MatCardModule } from "@angular/material/card";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

// Services and Models
import { AuthService } from "../../services/auth.service";
import { AuthRequest } from "../../models/auth.request.model";
import { MatDialog, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { AlertDialog } from "../../../../shared/alert-dialog/alert-dialog";

/** Login Component */
@Component({
  selector: "app-login",
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
  templateUrl: "./login.html",
  styleUrl: "./login.scss",
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router: Router = new Router();
  private readonly dialog = inject(MatDialog);

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly message = this.authService.message;

  loginForm: FormGroup = this.fb.group({
    email: ["", [Validators.required, Validators.email]],
    password: ["", Validators.required],
  });

  loading = false;
  showPassword = false;

  get email(): AbstractControl | null {
    return this.loginForm.get("email");
  }

  get password(): AbstractControl | null {
    return this.loginForm.get("password");
  }

  register(): void {
    this.authService.clearMessage();
    this.router.navigate(["/register"]);
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const payload: AuthRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password,
    };

    this.authService
      .login(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loading = false;
          console.log("Login payload:", payload);
          this.authService.clearMessage();
          this.router.navigate(["/files"]);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          console.error("Login error:", err);
          if (!err?.error) {
            this.dialog.open(AlertDialog, {
              panelClass: "rounded-dialog",
              data: {
                title: "Error",
                message: "Login failed. Please try again.",
              },
            });
          }
        },
      });
  }
}
export class DialogData {
  data = inject(MAT_DIALOG_DATA);
}
