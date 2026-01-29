import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { AuthRequest } from '../models/auth-request.model';
import { AuthResponse } from '../models/auth-response.model';
import { User } from '../models/user.model';
import { Router } from '@angular/router';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly config = inject(ConfigService);
  private readonly API_URL = this.config.getEndpointUrl('auth');

  // Signals for state management
  currentUser = signal<User | null>(null);
  isAuthenticated = signal<boolean>(false);
  message = signal<string | null>(null);
  errorStatus = signal<number | null>(null);
  errorPath = signal<string | null>(null);
  errorTimestamp = signal<string | null>(null);

  constructor() {
    this.checkAuthStatus();
  }
  /**
   * POST /auth/register
   *
   * @param request authentication request
   * @returns auth response
   */
  register(request: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/register`, request, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          this.message.set(response.message || 'Registration successful');
        }),
        catchError((error) => {
          this.message.set(
            error.error?.errors?.email ||
              error.error?.errors?.password ||
              error.error?.message ||
              'Registration failed'
          );
          this.errorStatus.set(error.error?.status || null);
          this.errorPath.set(error.error?.path || null);
          this.errorTimestamp.set(error.error?.timestamp || null);

          return throwError(() => error);
        })
      );
  }

  /**
   * POST /auth/login
   *
   * @param request authentication request
   * @returns auth response
   */
  login(request: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/login`, request, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          this.isAuthenticated.set(true);
          this.currentUser.set({ email: response.email || request.email });
          this.message.set(response.message || 'Login successful');
        }),
        catchError((error) => {
          this.message.set(
            error.error?.errors?.email ||
              error.error?.errors?.password ||
              error.error?.message ||
              'Login failed'
          );
          this.errorStatus.set(error.error?.status || null);
          this.errorPath.set(error.error?.path || null);
          this.errorTimestamp.set(error.error?.timestamp || null);
          this.isAuthenticated.set(false);

          return throwError(() => error);
        })
      );
  }

  /**
   * POST /auth/logout
   *
   * @returns empty observable
   */
  logout(): void {
    this.http
      .post<void>(`${this.API_URL}/logout`, {}, { withCredentials: true })
      .pipe(
        catchError((error) => {
          // Logout localy even if error
          this.isAuthenticated.set(false);
          this.currentUser.set(null);
          return throwError(() => error);
        })
      )
      .subscribe({
        next: () => {
          this.isAuthenticated.set(false);
          this.currentUser.set(null);
          this.message.set(null);
          this.errorStatus.set(null);
          this.errorPath.set(null);
          this.errorTimestamp.set(null);
          this.router.navigate(['/home']);
        },
        error: () => {
          this.message.set(null);
          this.errorStatus.set(null);
          this.errorPath.set(null);
          this.errorTimestamp.set(null);
          this.router.navigate(['/home']);
        },
      });
  }

  /**
   * GET /auth/me
   *
   * Check the authentication status
   */
  checkAuthStatus(): void {
    this.http
      .get<User>(`${this.API_URL}/me`, {
        withCredentials: true,
      })
      .subscribe({
        next: (user) => {
          this.isAuthenticated.set(true);
          this.currentUser.set(user);
        },
        error: () => {
          this.isAuthenticated.set(false);
          this.currentUser.set(null);
        },
      });
  }

  /**
   * Reset error message
   */
  clearError(): void {
    this.message.set(null);
  }
}
