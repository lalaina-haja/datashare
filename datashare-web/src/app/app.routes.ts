import { Routes } from "@angular/router";
import { Home } from "./shared/home/home";
import { Register } from "./features/auth/components/register/register";
import { Login } from "./features/auth/components/login/login";
import { Upload } from "./features/files/components/upload/upload";
import { Files } from "./features/files/components/files/files";

export const routes: Routes = [
  {
    path: "",
    component: Home,
  },
  {
    path: "home",
    component: Home,
  },
  {
    path: "register",
    component: Register,
  },
  {
    path: "login",
    component: Login,
  },
  {
    path: "upload",
    component: Upload,
  },
  {
    path: "files",
    component: Files,
  },
];
