import { render, screen, fireEvent } from '@testing-library/angular';
import { describe, it, expect, vi } from 'vitest';
import { of } from 'rxjs';
import { AuthService } from '../../../app/core/services/auth.service';
import { Register } from '../../../app/pages/register/register';

/**
 * Register Page Unit tests
 */
describe('Register Page', () => {
  /** TEST : ensure the form data are sent to AuthService.register() when user clicks on register button. */
  it('soumet le formulaire avec email et password', async () => {
    // GIVEN the authentication service (mock)
    const mockAuth = {
      register: vi.fn().mockReturnValue(of({ success: true })),
    };
    await render(Register, {
      providers: [{ provide: AuthService, useValue: mockAuth }],
    });

    // AND email field value is set
    fireEvent.input(screen.getByLabelText(/email/i), {
      target: { value: 'test@mail.com' },
    });

    // AND password field value is set
    fireEvent.input(screen.getByLabelText(/password/i), {
      target: { value: '123456' },
    });

    // WHEN click on register button
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    // THEN the service is called with the wight values
    expect(mockAuth.register).toHaveBeenCalledWith({
      email: 'test@mail.com',
      password: '123456',
    });
  });
});
