// src/app/features/auth/services/auth.service.ts

import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { catchError, Observable, of, take, tap, throwError } from "rxjs";

import { ConfigService } from "../../../core/services/config.service";
import { UserSignals } from "../../../core/signals/user.signals";
import { MessageSignals } from "../../../core/signals/message.signals";
import { AuthRequest } from "../models/auth.request.model";
import { AuthResponse } from "../models/auth.response.model";
import { User } from "../../../core/models/user.model";

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
  private readonly userSignals = new UserSignals();
  private readonly messageSignals = new MessageSignals();

  /** Published signals */
  user = this.userSignals.user;
  isAuthenticated = this.userSignals.isAuthenticated;
  message = this.messageSignals.message;
  errorStatus = this.messageSignals.errorStatus;
  errorPath = this.messageSignals.errorPath;
  errorTimestamp = this.messageSignals.errorTimestamp;

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
          this.messageSignals.success(
            response.message || "Registration successful",
          );
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Registration failed");

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
          this.messageSignals.success(response.message || "Login successful");
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Login failed");

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
          this.messageSignals.clear();

          this.router.navigate(["/home"]);
        }),
        catchError((error) => {
          this.user.set(null);
          this.messageSignals.error(error, "Logout error");

          return throwError(() => error);
        }),
      );
  }

  constructor() {
    this.checkAuthStatus();
  }

  checkAuthStatus(): Observable<User | null> {
    return this.http
      .get<User | null>(this.config.getEndpointUrl("me"), {
        withCredentials: true,
      })
      .pipe(
        tap((user) => {
          this.user.set(user);
        }),
        catchError(() => {
          this.user.set(null);
          return of(null);
        }),
        take(1),
      );
  }

  /**
   * Reset message
   */
  clearMessage(): void {
    this.messageSignals.clear();
  }
}
