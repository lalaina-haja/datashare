import { Component, inject } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService } from "../../features/auth/services/auth.service";

// Angular Material modules
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { CommonModule } from "@angular/common";
import { MatMenuModule } from "@angular/material/menu";
import { MatBadgeModule } from "@angular/material/badge";

@Component({
  selector: "app-header",
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    CommonModule,
    MatMenuModule,
    MatBadgeModule,
  ],
  templateUrl: "./header.html",
  styleUrl: "./header.scss",
})
export class Header {
  private readonly authService = inject(AuthService);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;

  showHistory = false;

  home(): void {
    this.router.navigate(["/home"]);
  }

  upload(): void {
    this.router.navigate(["/files/upload"]);
  }

  history(): void {
    this.router.navigate(["/files"]);
  }

  login(): void {
    this.authService.clearMessage();
    this.router.navigate(["/login"]);
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
