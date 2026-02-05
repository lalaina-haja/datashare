import { HttpInterceptorFn, HttpErrorResponse } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../features/auth/services/auth.service";
import { Router } from "@angular/router";
import { tap } from "rxjs/internal/operators/tap";

/**
 * Authentication Errors Interceptor:
 * - 401 → session expired → logout local
 * - 403 → access denied → redirection
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    tap({
      error: (err) => {
        if (err instanceof HttpErrorResponse) {
          if (err.status === 401) {
            // Session expired
            auth.user.set(null);
            router.navigate(["/home"]);
          }

          if (err.status === 403) {
            router.navigate(["/home", "access-denied"]);
          }
        }
      },
    }),
  );
};
