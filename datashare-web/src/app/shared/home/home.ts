import { Component, inject } from "@angular/core";

// Angular Material modules
import { MatGridListModule } from "@angular/material/grid-list";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatDialog } from "@angular/material/dialog";
import { FormsModule } from "@angular/forms";

// Services and shared components
import { AuthService } from "../../features/auth/services/auth.service";
import { AlertDialog } from "../dialog/components/alert-dialog/alert-dialog";
import { Router } from "@angular/router";
import { FileService } from "../../features/files/services/file.service";

@Component({
  selector: "app-home",
  imports: [
    MatCardModule,
    MatButtonModule,
    MatGridListModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
  ],
  templateUrl: "./home.html",
  styleUrl: "./home.scss",
})
export class Home {
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  downloadToken: string | null = null;

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;

  upload() {
    this.fileService.clearMessage();
    this.router.navigate(["/files/upload"]);
  }

  notImplemented() {
    this.dialog.open(AlertDialog, {
      panelClass: "rounded-dialog",
      data: {
        title: "⚠ Telechargement anonyme non disponible",
        message: "Veuillez vous connecter pour partager un fichier",
      },
    });
  }

  goToDownload() {
    if (this.downloadToken && this.downloadToken.trim().length > 0) {
      this.router.navigate([`/d/${this.downloadToken.trim()}`]);
    }
  }
}
