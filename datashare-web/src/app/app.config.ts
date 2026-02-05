// src/app/app.config.ts

import { ApplicationConfig, inject } from "@angular/core";
import { provideRouter } from "@angular/router";
import { routes } from "./app.routes";
import { environment } from "../../config/env/environment";

import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withXsrfConfiguration,
} from "@angular/common/http";

import { authInterceptor } from "./core/interceptors/auth.interceptor";
import { authCookieInterceptor } from "./core/interceptors/auth-cookie.interceptor";

import { AuthService } from "./features/auth/services/auth.service";

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    provideHttpClient(
      withFetch(),
      withXsrfConfiguration({
        cookieName: "XSRF-TOKEN",
        headerName: "X-XSRF-TOKEN",
      }),
      withInterceptors([authCookieInterceptor, authInterceptor]),
    ),

    // Expose environment variables for testing (e.g., Cypress)
    {
      provide: "ENVIRONMENT",
      useValue: environment,
    },

    // Initialise the authentication at startup
    {
      provide: "APP_INIT",
      multi: true,
      useFactory: () => {
        const auth = inject(AuthService);
        return () => auth.init();
      },
    },
  ],
};
