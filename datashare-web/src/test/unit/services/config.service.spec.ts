import { describe, it, expect, beforeEach, vi, beforeAll } from 'vitest';
import { ConfigService } from '../../../app/core/services/config.service';
import * as environment from '../../../environments/environment';

const API_URL = 'http://mock-api:8080';
const WEB_URL = 'http://mock-api:4200';
const TIMEOUT = 300;
const APPNAME = 'DataShare';
const PROD = false;

beforeAll(() => {
  vi.spyOn(environment, 'environment', 'get').mockReturnValue({
    production: PROD,
    apiUrl: API_URL,
    baseUrl: WEB_URL,
    timeout: TIMEOUT,
    appName: APPNAME,
  });
});

describe('ConfigService', () => {
  let service: ConfigService;

  beforeEach(() => {
    service = new ConfigService();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return API base URL', () => {
    expect(service.apiBaseUrl).toBe(API_URL);
  });

  it('should return complete URL', () => {
    expect(service.getEndpointUrl('auth')).toBe(`${API_URL}/auth`);
    expect(service.getEndpointUrl('files')).toBe(`${API_URL}/files`);
    expect(service.getEndpointUrl('users')).toBe(`${API_URL}/users`);
  });

  it('should return API timeout', () => {
    expect(service.apiTimeout).toBe(TIMEOUT);
  });

  it('should return application name', () => {
    expect(service.appName).toBe(APPNAME);
  });

  it('should return production flag', () => {
    expect(service.isProduction).toBe(PROD);
  });
});
