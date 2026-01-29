import { render, screen } from '@testing-library/angular';
import { describe, it, expect } from 'vitest';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Login } from '../../../app/pages/login/login';
import { AuthService } from '../../../app/core/services/auth.service';

describe('Login', () => {
  it('should call login with valid form', async () => {
    const loginSpy = vi.fn().mockReturnValue(of({ email: 'test@example.com' }));

    const authServiceMock = {
      login: loginSpy,
    };

    await render(Login, {
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    });

    const emailInput = screen.getByLabelText('Email') as HTMLInputElement;
    const passwordInput = screen.getByLabelText('Password') as HTMLInputElement;
    const button = screen.getByRole('button', { name: /login/i });

    emailInput.value = 'test@example.com';
    passwordInput.value = '123456';
    button.click();

    expect(loginSpy).toHaveBeenCalledWith('test@example.com', '123456');
  });
});
