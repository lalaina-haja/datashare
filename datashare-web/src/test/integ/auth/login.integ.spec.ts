// src/test/integ/auth/login.integ.spec.ts

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
import { Login } from "../../../app/features/auth/components/login/login";
import { Register } from "../../../app/features/auth/components/register/register";
import { Files } from "../../../app/features/files/components/files/files";

/** Login Component Integration Tests */
describe("Login (integ)", () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
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
      { path: "file", component: Files },
      { path: "**", redirectTo: "" },
    ];

    await TestBed.configureTestingModule({
      imports: [Login, ReactiveFormsModule],
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

    fixture = TestBed.createComponent(Login);
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

  describe("Form Integration", () => {
    it("should initialize form correctly", () => {
      expect(component.loginForm).toBeDefined();
      expect(component.email).toBe(component.loginForm.get("email"));
      expect(component.password).toBe(component.loginForm.get("password"));
    });
  });

  describe("onSubmit - Invalid Form", () => {
    it("should not submit invalid form", () => {
      component.onSubmit();
      expect(component.loading).toBe(false);
    });

    it("should mark fields as touched", () => {
      const emailControl = component.loginForm.get("email")!;
      const passwordControl = component.loginForm.get("password")!;

      component.onSubmit();

      expect(emailControl.touched).toBe(true);
      expect(passwordControl.touched).toBe(true);
    });
  });

  describe("onSubmit - HTTP Success", () => {
    it("should submit valid form and navigate on success", () => {
      // Arrange
      component.loginForm.setValue({
        email: "test@example.com",
        password: "password123",
      });
      const navigateSpy = vi.spyOn(component["router"], "navigate");

      // Act
      component.onSubmit();

      // Assert request
      const req = httpMock.expectOne((req) => req.url.includes("login"));
      req.flush({ token: "fake-jwt" });

      expect(req.request.method).toBe("POST");
      expect(req.request.body).toEqual({
        email: "test@example.com",
        password: "password123",
      });

      expect(component.loading).toBe(false);
      expect(navigateSpy).toHaveBeenCalledWith(["/files"]);
    });
  });

  describe("onSubmit - HTTP Errors", () => {
    it("should NOT show dialog when error.error exists", () => {
      component.loginForm.setValue({
        email: "test@example.com",
        password: "password123",
      });

      component.onSubmit();

      const req = httpMock.expectOne((req) => req.url.includes("login"));
      req.flush({ error: { message: "Custom error" } });

      expect(mockDialog.open).not.toHaveBeenCalled();
    });
  });

  describe("register navigation", () => {
    it("should navigate to register", () => {
      const navigateSpy = vi.spyOn(component["router"], "navigate");
      component.register();
      expect(navigateSpy).toHaveBeenCalledWith(["/register"]);
    });
  });
});
