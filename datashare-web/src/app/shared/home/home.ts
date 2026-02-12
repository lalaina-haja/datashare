import { Component, inject } from "@angular/core";

// Angular Material modules
import { MatGridListModule } from "@angular/material/grid-list";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";

// Services and shared components
import { AuthService } from "../../features/auth/services/auth.service";
import { Router } from "@angular/router";
import { FileService } from "../../features/files/services/file.service";

@Component({
  selector: "app-home",
  imports: [MatCardModule, MatButtonModule, MatGridListModule, MatIconModule],
  templateUrl: "./home.html",
  styleUrl: "./home.scss",
})
export class Home {
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);
  private readonly router: Router = new Router();

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;

  upload() {
    this.fileService.clearMessage();
    this.router.navigate(["/files/upload"]);
  }
}
