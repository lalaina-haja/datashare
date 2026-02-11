import { Component, inject, DestroyRef, OnInit, signal } from "@angular/core";
import { FileService } from "../../services/file.service";
import { ActivatedRoute } from "@angular/router";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { MatCardModule } from "@angular/material/card";
import { PresignedDownload } from "../../models/presigned-download.model";
import { HttpErrorResponse } from "@angular/common/http";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";

@Component({
  selector: "app-download",
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
  ],
  templateUrl: "./download.html",
  styleUrl: "./download.scss",
})
export class Download implements OnInit {
  private readonly fileService = inject(FileService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);

  /** Published signals */
  message = this.fileService.message;
  error = this.fileService.errorStatus;
  file = signal<PresignedDownload | null>(null);

  ngOnInit() {
    this.clearSignals();

    this.route.paramMap.subscribe((params) => {
      const token = params.get("token")!;
      console.log("Token:", token);

      this.fileService
        .getPresignedDownloadUrl(token)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (response: PresignedDownload) => {
            this.file.set(response);
          },
          error: (err: HttpErrorResponse) => {
            console.error("Download error:", err);
          },
        });
    });
  }

  clearSignals(): void {
    this.fileService.clearMessage();
    this.file.set(null);
  }

  getFileIcon(): string {
    const type = this.file()?.contentType;
    if (type?.startsWith("image/")) return "image";
    if (type?.startsWith("video/")) return "movie";
    if (type?.startsWith("audio/")) return "audiotrack";
    if (type?.includes("pdf")) return "picture_as_pdf";

    return "insert_drive_file";
  }

  download(): void {
    window.open(this.file()?.downloadUrl!, "_blank");
  }

  isActive(file: PresignedDownload): boolean {
    return new Date(file.expiresAt) > new Date();
  }

  formatSize(bytes: number): string {
    if (bytes === 0) return "0 B";

    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
  }

  messageExpiration(file: PresignedDownload): string {
    const target = new Date(file.expiresAt);
    const now = new Date();
    const diffMs = target.getTime() - now.getTime();
    const seconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);

    if (seconds < 1) {
      return "Ce fichier n'est plus disponible en téléchargement car il a expiré";
    }

    if (seconds < 60) {
      return "Ce fichier expirera dans quelques secondes";
    }
    if (minutes < 60) {
      return `Ce fichier expirera dans ${minutes} minute${minutes > 1 ? "s" : ""}`;
    }
    if (hours < 24) {
      return `Ce fichier expirera dans ${hours} heure${hours > 1 ? "s" : ""}`;
    }
    if (days == 1) {
      return `Ce fichier expirera demain`;
    }
    if (days < 7) {
      return `Ce fichier expirera dans ${days} jour${days > 1 ? "s" : ""}`;
    }
    return `Ce fichier expirera dans ${weeks} semaine${weeks > 1 ? "s" : ""}`;
  }

  truncateFileName(filename: string, maxLength: number = 10): string {
    // Si déjà court, retourne tel quel
    if (filename.length <= maxLength) return filename;

    const dotIndex = filename.lastIndexOf(".");
    if (dotIndex === -1) {
      // Pas d'extension → simple troncature
      return filename.substring(0, maxLength) + "...";
    }

    const name = filename.substring(0, dotIndex);
    const ext = filename.substring(dotIndex);

    // Préserve extension si possible
    if (name.length <= maxLength - ext.length - 3) {
      return filename;
    }

    return name.substring(0, maxLength - ext.length - 3) + "..." + ext;
  }
}
