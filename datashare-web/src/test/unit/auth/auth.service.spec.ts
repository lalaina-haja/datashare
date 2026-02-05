import { TestBed } from "@angular/core/testing";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { ConfigService } from "../../../app/core/services/config.service";
import { Router } from "@angular/router";
import { AuthRequest } from "../../../app/features/auth/models/auth.request.model";
import { AuthResponse } from "../../../app/features/auth/models/auth.response.model";
import { User } from "../../../app/core/models/user.model";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";

import {
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";

/**
 * AuthService Unit Tests
 */
describe("AuthService (unit)", () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let mockConfigService: { getEndpointUrl: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockConfigService = {
      getEndpointUrl: vi.fn(),
    };

    mockRouter = {
      navigate: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: ConfigService, useValue: mockConfigService },
        { provide: Router, useValue: mockRouter },

        // HttpClient + testing
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      const httpMock = TestBed.inject(HttpTestingController);
      httpMock.verify();
    } catch (e) {
      // Ignore if there is no HttpTestingController in the TestBed
    }
  });

  // -------------------------------------------------------
  // REGISTER
  // -------------------------------------------------------
  describe("register()", () => {
    it("should call backend and set success message", () => {
      const payload: AuthRequest = { email: "test@test.com", password: "1234" };
      const mockResponse: AuthResponse = {
        message: "Registered",
      } as AuthResponse;
      (mockConfigService.getEndpointUrl as any).mockReturnValue(
        "/auth/register",
      );

      service.register(payload).subscribe((res) => {
        expect(res).toEqual(mockResponse);
        expect(service.message()).toBe("Registered");
      });

      const req = httpMock.expectOne("/auth/register");
      expect(req.request.method).toBe("POST");
      expect(req.request.withCredentials).toBe(true);
      req.flush(mockResponse);
    });

    it("should set error signals in case of failure", () => {
      (mockConfigService.getEndpointUrl as any).mockReturnValue(
        "/auth/register",
      );
      const payload: AuthRequest = { email: "", password: "" };
      const mockError = {
        message: "Invalid email",
        status: 400,
        path: "/auth/register",
        timestamp: "now",
      };

      service.register(payload).subscribe({
        error: (err) => {
          expect(err.status).toBe(400);
          expect(service.message()).toBe("Invalid email");
          expect(service.errorStatus()).toBe(400);
          expect(service.errorPath()).toBe("/auth/register");
          expect(service.errorTimestamp()).toBe("now");
        },
      });

      const req = httpMock.expectOne("/auth/register");
      req.flush(mockError);
    });
  });

  // -------------------------------------------------------
  // LOGIN
  // -------------------------------------------------------
  describe("login()", () => {
    it("should update user and message in case of success", () => {
      const payload: AuthRequest = { email: "a@b.com", password: "pwd" };
      const mockResponse: AuthResponse = {
        message: "Welcome back",
        email: "a@b.com",
        authorities: ["USER"],
      } as AuthResponse;

      (mockConfigService.getEndpointUrl as any).mockReturnValue("/auth/login");

      service.login(payload).subscribe((res) => {
        expect(res).toEqual(mockResponse);
        expect(service.user()).toEqual({
          email: "a@b.com",
          authorities: ["USER"],
        } as User);
        expect(service.message()).toBe("Welcome back");
      });

      const req = httpMock.expectOne("/auth/login");
      expect(req.request.method).toBe("POST");
      expect(req.request.withCredentials).toBe(true);
      req.flush(mockResponse);
    });

    it("should set error signals in case of failure", () => {
      (mockConfigService.getEndpointUrl as any).mockReturnValue("/auth/login");

      const mockError = {
        message: "Unauthorized",
        status: 401,
        path: "/auth/login",
        timestamp: "now",
      };

      service.login({ email: "", password: "" }).subscribe({
        error: () => {
          expect(service.message()).toBe("Unauthorized");
          expect(service.errorStatus()).toBe(401);
          expect(service.errorPath()).toBe("/auth/login");
          expect(service.errorTimestamp()).toBe("now");
        },
      });

      const req = httpMock.expectOne("/auth/login");
      req.flush(mockError);
    });
  });

  // -------------------------------------------------------
  // LOGOUT
  // -------------------------------------------------------
  describe("logout()", () => {
    it("should clear signals on success", () => {
      mockConfigService.getEndpointUrl.mockReturnValue("/auth/logout");

      service.logout();

      expect(service.user()).toBeNull();
      expect(service.message()).toBeNull();
      expect(service.errorStatus()).toBeNull();
      expect(service.errorPath()).toBeNull();
      expect(service.errorTimestamp()).toBeNull();
    });
  });

  // -------------------------------------------------------
  // ME
  // -------------------------------------------------------
  describe("me()", () => {
    it("should set user if session is valid", () => {
      const mockUser: User = { email: "me@test.com", authorities: ["USER"] };
      (mockConfigService.getEndpointUrl as any).mockReturnValue("/auth/me");

      service.me().subscribe(() => {
        expect(service.user()).toEqual(mockUser);
      });

      const req = httpMock.expectOne("/auth/me");
      expect(req.request.method).toBe("GET");
      expect(req.request.withCredentials).toBe(true);
      req.flush(mockUser);
    });

    it("should set user to null in case of error", () => {
      (mockConfigService.getEndpointUrl as any).mockReturnValue("/auth/me");

      service.me().subscribe({
        error: () => {
          expect(service.user()).toBeNull();
        },
      });

      const req = httpMock.expectOne("/auth/me");
      req.flush({
        message: "Unauthorized",
        status: 401,
        path: "/auth/me",
        timestamp: "now",
      });
    });
  });

  // -------------------------------------------------------
  // INIT
  // -------------------------------------------------------
  describe("init()", () => {
    it("should call me() on initialization", () => {
      const meSpy = vi.spyOn(service, "me").mockReturnValue({
        subscribe: vi.fn(),
      } as any);

      service.init();

      expect(meSpy).toHaveBeenCalled();
    });
  });
});
