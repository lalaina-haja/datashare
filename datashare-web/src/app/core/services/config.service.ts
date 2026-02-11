import { Injectable } from "@angular/core";
import { environment } from "../../../../config/env/environment";

export interface ApiEndpoints {
  register: string;
  login: string;
  logout: string;
  me: string;
  files: string;
  upload: string;
  download: string;
}

@Injectable({
  providedIn: "root",
})
export class ConfigService {
  private readonly config = {
    api: {
      baseUrl: environment.apiUrl,
      timeout: environment.timeout,
      endpoints: {
        register: "/auth/register",
        login: "/auth/login",
        logout: "/auth/logout",
        me: "/auth/me",
        files: "/files",
        upload: "/files/upload",
        download: "/files/download",
      } as const,
    },
    app: {
      baseUrl: environment.baseUrl,
      name: environment.appName,
      production: environment.production,
    },
  };

  /**
   * WEB base URL
   */
  get webBaseUrl(): string {
    return this.config.app.baseUrl;
  }

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
