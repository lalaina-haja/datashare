import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../features/auth/services/auth.service";
import { catchError, throwError } from "rxjs";
import { ConfigService } from "../services/config.service";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const config = inject(ConfigService);
  const isAPI = req.url.startsWith(config.apiBaseUrl);

  const authReq = isAPI
    ? req.clone({ withCredentials: true })
    : req.clone({ withCredentials: false });

  return next(authReq).pipe(
    catchError((err) => {
      if (err.status === 401 && isAPI) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
