// src/app/app.config.ts

import { ApplicationConfig } from "@angular/core";
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
import { MatPaginatorIntl } from "@angular/material/paginator";
import { frenchPaginatorIntl } from "./core/trads/material-paginator-intl";

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    provideHttpClient(
      withFetch(),
      withXsrfConfiguration({
        cookieName: "XSRF-TOKEN",
        headerName: "X-XSRF-TOKEN",
      }),
      withInterceptors([authInterceptor, authInterceptor]),
    ),
    {
      provide: "ENVIRONMENT",
      useValue: environment,
    },
    { provide: MatPaginatorIntl, useFactory: frenchPaginatorIntl },
  ],
};
