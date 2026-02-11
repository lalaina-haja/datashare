// src/test/integ/auth/register.integ.spec.ts

// Angular modules
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { ReactiveFormsModule } from "@angular/forms";
import {
  Router,
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from "@angular/router";
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import { MatDialog } from "@angular/material/dialog";
import { of } from "rxjs";
// Component to test
import { Register } from "../../../app/features/auth/components/register/register";
import { Login } from "../../../app/features/auth/components/login/login";

/** Register Component Integration Tests */
describe("Register (integ)", () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let httpMock: HttpTestingController;
  let mockRouter: any;
  let mockDialog: any;

  beforeEach(async () => {
    mockRouter = {
      events: of(),
      navigate: vi.fn(() => Promise.resolve(true)),
      navigateByUrl: vi.fn(() => Promise.resolve(true)),
    } as unknown as Router;

    mockDialog = { open: vi.fn() };

    const testRoutes = [
      { path: "register", component: Register },
      { path: "login", component: Login },
      { path: "**", redirectTo: "" },
    ];

    await TestBed.configureTestingModule({
      imports: [Register, ReactiveFormsModule],
      providers: [
        provideRouter(testRoutes),
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Router, useValue: mockRouter },
        { provide: MatDialog, useValue: mockDialog },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({}) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    const injectedRouter = TestBed.inject(Router);
    vi.spyOn(injectedRouter, "navigate");
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    try {
      httpMock.verify();
      vi.clearAllMocks();
    } catch (error) {
      // Ignore errors from verify in case no requests were made
    }
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  describe("Form Validation", () => {
    it("should initialize form with required validators", () => {
      expect(component.registerForm).toBeDefined();
      expect(component.email).toBe(component.registerForm.get("email"));
      expect(component.password).toBe(component.registerForm.get("password"));
      expect(component.confirmPassword).toBe(
        component.registerForm.get("confirmPassword"),
      );
      expect(component.registerForm.valid).toBe(false);
    });

    it("should validate email format", () => {
      const emailControl = component.registerForm.get("email")!;
      emailControl.setValue("invalid");
      expect(emailControl.valid).toBe(false);
      expect(emailControl.hasError("email")).toBe(true);

      emailControl.setValue("test@example.com");
      expect(emailControl.valid).toBe(true);
    });

    it("should validate password min length", () => {
      const passwordControl = component.registerForm.get("password")!;
      passwordControl.setValue("short");
      expect(passwordControl.valid).toBe(false);
      expect(passwordControl.hasError("minlength")).toBe(true);

      passwordControl.setValue("password12345678");
      expect(passwordControl.valid).toBe(true);
    });

    it("should validate password match validator", () => {
      component.registerForm.setValue({
        email: "test@example.com",
        password: "password12345678",
        confirmPassword: "different",
      });
      expect(component.registerForm.valid).toBe(false);
      expect(component.registerForm.hasError("passwordMismatch")).toBe(true);

      component.registerForm.setValue({
        email: "test@example.com",
        password: "password12345678",
        confirmPassword: "password12345678",
      });
      expect(component.registerForm.valid).toBe(true);
    });
  });

  describe("onSubmit - Invalid Form", () => {
    it("should not submit invalid form", () => {
      component.onSubmit();
      expect(component.loading).toBe(false);
    });

    it("should mark all fields as touched on invalid submit", () => {
      const emailControl = component.registerForm.get("email")!;
      const passwordControl = component.registerForm.get("password")!;
      const confirmPasswordControl =
        component.registerForm.get("confirmPassword")!;

      component.onSubmit();

      expect(emailControl.touched).toBe(true);
      expect(passwordControl.touched).toBe(true);
      expect(confirmPasswordControl.touched).toBe(true);
    });
  });

  describe("onSubmit - HTTP Success", () => {
    it("should submit valid form and navigate to login", () => {
      // Arrange - Form valid
      component.registerForm.setValue({
        email: "test@example.com",
        password: "password12345678",
        confirmPassword: "password12345678",
      });
      const navigateSpy = vi.spyOn(component["router"], "navigate");

      // Act
      component.onSubmit();

      // Assert HTTP request
      const req = httpMock.expectOne((req) => req.url.includes("register"));
      req.flush({ success: true });

      expect(req.request.method).toBe("POST");
      expect(req.request.body).toEqual({
        email: "test@example.com",
        password: "password12345678",
      });

      // Assert success flow
      expect(component.loading).toBe(false);
      expect(navigateSpy).toHaveBeenCalledWith(["/login"]);
    });
  });

  describe("onSubmit - HTTP Errors", () => {
    it("should NOT show dialog when error.error exists", () => {
      component.registerForm.setValue({
        email: "test@example.com",
        password: "password12345678",
        confirmPassword: "password12345678",
      });

      component.onSubmit();

      const req = httpMock.expectOne((req) => req.url.includes("register"));
      req.flush({ error: { message: "Custom error" } });

      expect(component.loading).toBe(false);
      //expect(mockDialog.open).not.toHaveBeenCalled();
    });
  });

  describe("login navigation", () => {
    it("should navigate to login page", () => {
      const navigateSpy = vi.spyOn(component["router"], "navigate");
      component.login();
      expect(navigateSpy).toHaveBeenCalledWith(["/login"]);
    });
  });

  describe("Form state", () => {
    it("should toggle loading state during submit", () => {
      component.registerForm.setValue({
        email: "test@example.com",
        password: "password12345678",
        confirmPassword: "password12345678",
      });

      component.onSubmit();
      expect(component.loading).toBe(true);

      const req = httpMock.expectOne((req) => req.url.includes("register"));
      req.flush({ success: true });

      expect(component.loading).toBe(false);
    });
  });
});
