import { describe, it, expect, beforeEach, vi, beforeAll, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../../../app/core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ConfigService } from '../../../app/core/services/config.service';
import { of, throwError, firstValueFrom } from 'rxjs';

describe('AuthService – unit', () => {
  let service: AuthService;
  let httpMock: { post: any; get: any };
  let routerMock: { navigate: any };

  beforeEach(() => {
    httpMock = {
      post: vi.fn(),
      get: vi.fn().mockReturnValue(throwError(() => new Error('Not authenticated'))),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: HttpClient, useValue: httpMock },
        { provide: Router, useValue: routerMock },
        {
          provide: ConfigService,
          useValue: {
            getEndpointUrl: () => 'http://localhost:3000/auth',
          },
        },
      ],
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  // ============================
  // register()
  // ============================
  it('should set message on successful registration', async () => {
    httpMock.post.mockReturnValue(of({ message: 'Registered' }));

    await firstValueFrom(service.register({ email: 'a@a.com', password: '1234' }));

    expect(service.message()).toBe('Registered');
  });

  it('should handle register error', async () => {
    httpMock.post.mockReturnValue(
      throwError(() => ({ error: { message: 'Email exists', status: 400 } }))
    );

    await expect(
      firstValueFrom(service.register({ email: 'a@a.com', password: '1234' }))
    ).rejects.toBeTruthy();

    expect(service.message()).toBe('Email exists');
    expect(service.errorStatus()).toBe(400);
  });

  // ============================
  // login()
  // ============================
  it('should authenticate user on login', async () => {
    httpMock.post.mockReturnValue(of({ email: 'a@a.com', message: 'Logged in' }));

    await firstValueFrom(service.login({ email: 'a@a.com', password: '1234' }));

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.email).toBe('a@a.com');
  });

  // ============================
  // logout()
  // ============================
  it('should reset state and navigate on logout', () => {
    httpMock.post.mockReturnValue(of(null));

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBe(null);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/home']);
  });

  // ============================
  // checkAuthStatus()
  // ============================
  it('should set isAuthenticated and currentUser on success', () => {
    const user = { email: 'test@test.com' };
    httpMock.get.mockReturnValue(of(user));

    service.checkAuthStatus();

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual(user);
  });

  it('should reset state on error', () => {
    httpMock.get.mockReturnValue(throwError(() => new Error('fail')));

    service.checkAuthStatus();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBe(null);
  });

  // ============================
  // clearError()
  // ============================
  it('should clear the message', () => {
    service.message.set('Error');
    service.clearError();
    expect(service.message()).toBe(null);
  });
});
