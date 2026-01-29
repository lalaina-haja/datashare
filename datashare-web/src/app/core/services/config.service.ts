import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface ApiEndpoints {
  auth: string;
  files: string;
  users: string;
}

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  private readonly config = {
    api: {
      baseUrl: environment.apiUrl,
      timeout: environment.timeout,
      endpoints: {
        auth: '/auth',
        files: '/files',
        users: '/users',
      } as const,
    },
    app: {
      name: environment.appName,
      production: environment.production,
    },
  };

  /**
   * API base URL
   */
  get apiBaseUrl(): string {
    return this.config.api.baseUrl;
  }

  /**
   * Timeout for HTTP requests (in ms)
   */
  get apiTimeout(): number {
    return this.config.api.timeout;
  }

  /**
   * Get complete endpoint URL
   */
  getEndpointUrl(endpoint: keyof ApiEndpoints): string {
    return `${this.config.api.baseUrl}${this.config.api.endpoints[endpoint]}`;
  }

  /**
   * Application name
   */
  get appName(): string {
    return this.config.app.name;
  }

  /**
   * Production mode
   */
  get isProduction(): boolean {
    return this.config.app.production;
  }
}
