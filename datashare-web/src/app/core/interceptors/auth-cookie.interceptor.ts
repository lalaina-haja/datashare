import { HttpInterceptorFn } from "@angular/common/http";
import { ConfigService } from "../services/config.service";
import { inject } from "@angular/core";

/**
 * Adds automatically withCredentials: true
 * for all requests to the backend API.
 */
export const authCookieInterceptor: HttpInterceptorFn = (req, next) => {
  const thisConfig = inject(ConfigService);
  const apiUrl = thisConfig.apiBaseUrl;

  // Update the request only if it's going to the API
  if (req.url.startsWith(apiUrl)) {
    const cloned = req.clone({
      withCredentials: true,
    });
    return next(cloned);
  }

  return next(req);
};
