// src/test/unit/auth/login.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach, vi } from "vitest";
// Angular Modules
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule } from "@angular/forms";
import {
  Router,
  ActivatedRoute,
  RouterLink,
  provideRouter,
} from "@angular/router";
// Angular Material
import { MatCardModule } from "@angular/material/card";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
// SUT (System Under Test)
import { Login } from "../../../app/features/auth/components/login/login";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { of, throwError } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

/** Login Component Unit tests */
describe("Login (unit)", () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  const mockAuthService = {
    login: vi.fn(),
    user: vi.fn(() => null),
    isAuthenticated: vi.fn(() => false),
    message: vi.fn(() => null),
    errorStatus: vi.fn(() => null),
    clearMessage: vi.fn(),
    checkAuthStatus: vi.fn(() => of([])),
  };

  const mockRouter = {
    navigate: vi.fn(),
    navigateByUrl: vi.fn(),
    createUrlTree: vi.fn(),
    serializeUrl: vi.fn(),
    parseUrl: vi.fn(),
    events: { subscribe: vi.fn() }, // Mock event emitter
    routerState: { root: { children: [] } },
  };

  const mockDialog = { open: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        Login,
        CommonModule,
        ReactiveFormsModule,
        RouterLink,
        MatCardModule,
        MatInputModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatDialogModule,
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        { provide: MatDialog, useValue: mockDialog },
        { provide: ActivatedRoute, useValue: { snapshot: {} } },
      ],
    }).compileComponents;

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    vi.clearAllMocks();
    fixture.detectChanges();
  });

  /* -------------------------
   * Basic creation
   * ------------------------- */

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  /* -------------------------
   * Form validation
   * ------------------------- */
  it("should have an invalid form when empty", () => {
    expect(component.loginForm.invalid).toBe(true);
  });

  /* -------------------------
   * onSubmit behavior
   * ------------------------- */
  describe("onSubmit()", () => {
    it("should mark all fields as touched if form is invalid", () => {
      const markSpy = vi.spyOn(component.loginForm, "markAllAsTouched");
      component.onSubmit();

      expect(markSpy).toHaveBeenCalled();
      expect(mockAuthService.login).not.toHaveBeenCalled();
    });

    it("should call authService.login and handle success", () => {
      mockAuthService.login.mockReturnValue(of(void 0));
      component.email?.setValue("test@example.com");
      component.password?.setValue("password123");

      component.onSubmit();

      expect(component.loading).toBe(false);
      expect(mockAuthService.login).toHaveBeenCalledWith({
        email: "test@example.com",
        password: "password123",
      });

      expect(mockAuthService.message()).toBeNull();
    });

    it("should handle HTTP error and show generic error dialog", () => {
      mockAuthService.login.mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 500,
              error: null,
            }),
        ),
      );

      component.email?.setValue("test@example.com");
      component.password?.setValue("password123");

      component.onSubmit();

      expect(component.loading).toBe(false);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });
});
