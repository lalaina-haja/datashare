import { describe, it, expect } from 'vitest';

const API_URL = process.env['API_URL']!;

describe('Auth API – integration', () => {
  it('POST /register', async () => {
    const res = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'user@test.com',
        password: 'password',
      }),
    });

    expect(res.status).toBe(201);
  });

  it('POST /login', async () => {
    const res = await fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'user@test.com',
        password: 'password',
      }),
    });

    const body = await res.json();
    expect(body.token).toBeDefined();
  });

  it('POST /logout', async () => {
    const res = await fetch(`${API_URL}/logout`, {
      method: 'POST',
    });

    expect(res.status).toBe(204);
  });
});
