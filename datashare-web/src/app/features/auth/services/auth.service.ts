// src/app/features/auth/services/auth.service.ts
import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { catchError, Observable, tap, throwError } from "rxjs";
import { ConfigService } from "../../../core/services/config.service";
import { AuthSignals } from "../signals/auth.signal";
import { AuthRequest } from "../models/auth.request.model";
import { AuthResponse } from "../models/auth.response.model";
import { User } from "../../../core/models/user.model";
import { Router } from "@angular/router";

/**
 * AuthService
 * -----------
 * Authentication Service :
 * - register
 * - login
 * - logout
 * - me (session verification)
 *
 * Compatible :
 * - Cookies HttpOnly (session)
 * - XSRF automatique (Angular 21)
 * - Signals (AuthSignals)
 */
@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly config = inject(ConfigService);
  private readonly signals = new AuthSignals();

  /** Published signals */
  user = this.signals.user;
  isAuthenticated = this.signals.isAuthenticated;
  message = this.signals.message;
  errorStatus = this.signals.errorStatus;
  errorPath = this.signals.errorPath;
  errorTimestamp = this.signals.errorTimestamp;

  /**
   * Register a new user
   */
  register(payload: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(this.config.getEndpointUrl("register"), payload, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          this.message.set(response.message || "Registration successful");
        }),
        catchError((error) => {
          this.message.set(
            error.error?.errors?.email ||
              error.error?.errors?.password ||
              error.error?.message ||
              "Registration failed",
          );
          this.errorStatus.set(error.error?.status || null);
          this.errorPath.set(error.error?.path || null);
          this.errorTimestamp.set(error.error?.timestamp || null);

          return throwError(() => error);
        }),
      );
  }

  /**
   * Login user
   * - The HttpOnly cookie is automatically set by the browser
   * - The backend returns an AuthResponse
   * - The user signal is updated
   */
  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(this.config.getEndpointUrl("login"), payload, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          const user: User = {
            email: response.email,
            authorities: response.authorities,
          };
          this.user.set(user);
          this.message.set(response.message || "Login successful");
        }),
        catchError((error) => {
          this.message.set(
            error.error?.errors?.email ||
              error.error?.errors?.password ||
              error.error?.message ||
              "Login failed",
          );
          this.errorStatus.set(error.error?.status || null);
          this.errorPath.set(error.error?.path || null);
          this.errorTimestamp.set(error.error?.timestamp || null);

          return throwError(() => error);
        }),
      );
  }

  /**
   * Logout user
   * - The backend deletes the HttpOnly cookie
   * - clears the user state
   */
  logout(): Observable<void> {
    return this.http
      .post<void>(
        this.config.getEndpointUrl("logout"),
        {},
        { withCredentials: true },
      )
      .pipe(
        tap(() => {
          this.user.set(null);
          this.message.set(null);
          this.errorStatus.set(null);
          this.errorPath.set(null);
          this.errorTimestamp.set(null);
          this.router.navigate(["/home"]);
        }),
        catchError((error) => {
          this.user.set(null);
          this.message.set(error.error?.message || "Logout error");
          this.errorStatus.set(error.error?.status || null);
          this.errorPath.set(error.error?.path || null);
          this.errorTimestamp.set(error.error?.timestamp || null);
          return throwError(() => error);
        }),
      );
  }

  /**
   * Checks the session at startup
   * - Calls /auth/me
   * - If valid cookie → update user signal
   * - Else → user = null
   */
  me() {
    return this.http
      .get<User>(this.config.getEndpointUrl("me"), { withCredentials: true })
      .pipe(
        tap({
          next: (user) => this.user.set(user),
          error: () => this.user.set(null),
        }),
      );
  }

  /**
   * Initialises authentication at application startup
   * To be called in main.ts or app.config.ts
   */
  init() {
    this.me().subscribe();
  }

  /**
   * Reset message
   */
  clearMessage(): void {
    this.message.set(null);
  }
}
