import { Routes } from "@angular/router";
import { Home } from "./shared/home/home";
import { Register } from "./features/auth/components/register/register";
import { Login } from "./features/auth/components/login/login";
import { Upload } from "./features/files/components/upload/upload";
import { Files } from "./features/files/components/files/files";
import { authGuard, authInitGuard } from "./core/guards/auth.guard";
import { Download } from "./features/files/components/download/download";

export const routes: Routes = [
  {
    path: "",
    canMatch: [authInitGuard],
    children: [
      {
        path: "",
        component: Home,
      },
      {
        path: "home",
        redirectTo: "",
        pathMatch: "full",
      },
      {
        path: "files",
        component: Files,
        canActivate: [authGuard],
      },
      {
        path: "files/upload",
        component: Upload,
        canActivate: [authGuard],
      },
      {
        path: "d/:token",
        component: Download,
        canActivate: [authGuard],
      },
    ],
  },
  {
    path: "login",
    component: Login,
  },
  {
    path: "register",
    component: Register,
  },
  {
    path: "**",
    redirectTo: "",
  },
];
