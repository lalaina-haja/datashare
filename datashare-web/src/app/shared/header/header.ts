import { Component, inject } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService } from "../../features/auth/services/auth.service";

// Angular Material modules
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";

@Component({
  selector: "app-header",
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatCardModule],
  templateUrl: "./header.html",
  styleUrl: "./header.scss",
})
export class Header {
  private readonly authService = inject(AuthService);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;

  login(): void {
    this.authService.clearMessage();
    this.router.navigate(["/login"]);
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
