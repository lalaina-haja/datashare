import { Component, inject } from "@angular/core";
import { AuthService } from "../../../auth/services/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: "app-files",
  imports: [],
  templateUrl: "./files.html",
  styleUrl: "./files.scss",
})
export class Files {
  private readonly authService = inject(AuthService);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly message = this.authService.message;

  constructor() {
    if (!this.isAuthenticated()) {
      this.router.navigate(["/home"]);
    }
  }
}
