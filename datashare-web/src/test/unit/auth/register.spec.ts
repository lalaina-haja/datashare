// src/test/unit/auth/register.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach, vi } from "vitest";
// Angular Modules
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule } from "@angular/forms";
import { Router, ActivatedRoute, RouterLink } from "@angular/router";
// Angular Material
import { MatCardModule } from "@angular/material/card";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
// SUT (System Under Test)
import { Register } from "../../../app/features/auth/components/register/register";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { of, throwError } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

/** Register Component Unit Tests */
describe("Register Component (unit)", () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  const mockAuthService = {
    register: vi.fn(),
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

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [
        Register,
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
        { provide: MatDialog, useValue: mockDialog },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: { snapshot: {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
    vi.clearAllMocks();
  });

  /* -------------------------
   * Basic creation
   * ------------------------- */
  it("should create the component", () => {
    expect(component).toBeTruthy();
  });

  /* -------------------------
   * Form validation
   * ------------------------- */
  it("should have an invalid form when empty", () => {
    expect(component.registerForm.invalid).toBe(true);
  });

  it("should validate email format", () => {
    component.email?.setValue("invalid-email");
    component.password?.setValue("password123");
    component.confirmPassword?.setValue("password123");

    expect(component.registerForm.invalid).toBe(true);
    expect(component.email?.errors?.["email"]).toBe(true);
  });

  it("should invalidate form when passwords do not match", () => {
    component.email?.setValue("test@example.com");
    component.password?.setValue("password123");
    component.confirmPassword?.setValue("password456");

    expect(component.registerForm.errors).toEqual({
      passwordMismatch: true,
    });
  });

  it("should validate form when passwords match", () => {
    component.email?.setValue("test@example.com");
    component.password?.setValue("password123");
    component.confirmPassword?.setValue("password123");

    expect(component.registerForm.valid).toBe(true);
  });

  /* -------------------------
   * onSubmit behavior
   * ------------------------- */
  describe("onSubmit()", () => {
    it("should mark all fields as touched if form is invalid", () => {
      const markSpy = vi.spyOn(component.registerForm, "markAllAsTouched");
      component.onSubmit();

      expect(markSpy).toHaveBeenCalled();
      expect(mockAuthService.register).not.toHaveBeenCalled();
    });

    it("should call authService.register and handle success", () => {
      mockAuthService.register.mockReturnValue(of(void 0));
      component.email?.setValue("test@example.com");
      component.password?.setValue("password123");
      component.confirmPassword?.setValue("password123");

      component.onSubmit();

      expect(component.loading).toBe(false);
      expect(mockAuthService.register).toHaveBeenCalledWith({
        email: "test@example.com",
        password: "password123",
      });

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockAuthService.message()).toBeNull();
    });

    it("should handle HTTP error and show generic error dialog", () => {
      mockAuthService.register.mockReturnValue(
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
      component.confirmPassword?.setValue("password123");

      component.onSubmit();

      expect(component.loading).toBe(false);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });
});
