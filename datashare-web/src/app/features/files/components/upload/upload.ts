import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { HttpErrorResponse, HttpEventType } from "@angular/common/http";

import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { ReactiveFormsModule } from "@angular/forms";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatSelectModule } from "@angular/material/select";
import { ClipboardModule } from "@angular/cdk/clipboard";

import { PresignedUpload } from "../../models/presigned-upload.model";
import { FileService } from "../../services/file.service";
import { MatInputModule } from "@angular/material/input";
import { AuthService } from "../../../auth/services/auth.service";
import { Router } from "@angular/router";
import { ConfigService } from "../../../../core/services/config.service";

@Component({
  selector: "app-upload",
  imports: [
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    ClipboardModule,
  ],
  templateUrl: "./upload.html",
  styleUrl: "./upload.scss",
})
export class Upload implements OnInit {
  private readonly fileService = inject(FileService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);
  private readonly configService = inject(ConfigService);
  private readonly router: Router = new Router();

  /** Published signals */
  file = signal<File | null>(null);
  progress = signal<number | null>(null);
  expirationDays = signal(7);
  message = this.fileService.message;
  error = this.fileService.errorStatus;
  fileExpiration = signal<string | null>(null);
  fileDownloadUrl = signal<string | null>(null);
  uploadSuccess = computed(() => this.fileDownloadUrl() !== null);

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(["/home"]);
    }
    this.fileService.clearMessage();
    this.fileDownloadUrl.set(null);
    this.fileExpiration.set(null);
  }

  triggerFileSelect(input: HTMLInputElement) {
    input.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    this.file.set(input.files[0]);
    this.progress.set(null);
  }

  upload() {
    const file = this.file();
    if (!file) return;

    this.fileService.clearMessage();

    this.fileService
      .getPresignedUploadUrl(file, this.expirationDays())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response: PresignedUpload) => {
          this.fileService
            .uploadToS3(response.uploadUrl, file)
            .subscribe((event) => {
              if (event.type === HttpEventType.UploadProgress && event.total) {
                const percent = Math.round((event.loaded / event.total) * 100);
                this.progress.set(percent);
              }
              if (event.type === HttpEventType.Response) {
                this.fileDownloadUrl.set(
                  `${this.configService.webBaseUrl}/d/${response.tokenString}`,
                );
                this.fileExpiration.set(this.humanize(response.expiresAt));
                console.log("Upload OK, url = ", response.uploadUrl);
              }
            });
        },
        error: (err: HttpErrorResponse) => {
          console.error("Upload error:", err);
        },
      });
  }

  getFileIcon(): string {
    const f = this.file();
    if (!f) return "insert_drive_file";
    if (f.type.startsWith("image/")) return "image";
    if (f.type.startsWith("video/")) return "movie";
    if (f.type.startsWith("audio/")) return "audiotrack";
    if (f.type.includes("pdf")) return "picture_as_pdf";

    return "insert_drive_file";
  }

  humanize(dateString: string): string {
    const target = new Date(dateString);
    const now = new Date();
    const diffMs = target.getTime() - now.getTime();
    const seconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);

    if (seconds < 60) {
      return "pendant quelques secondes";
    }
    if (minutes < 60) {
      return `pendant ${minutes} minute${minutes > 1 ? "s" : ""}`;
    }
    if (hours < 24) {
      return `pendant ${hours} heure${hours > 1 ? "s" : ""}`;
    }
    if (days < 7) {
      return `pendant ${days} jour${days > 1 ? "s" : ""}`;
    }
    return `pendant ${weeks} semaine${weeks > 1 ? "s" : ""}`;
  }
}
