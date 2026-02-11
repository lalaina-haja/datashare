import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
  ViewChild,
} from "@angular/core";
import { Router } from "@angular/router";
import { CommonModule } from "@angular/common";

import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatListModule } from "@angular/material/list";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatTooltipModule } from "@angular/material/tooltip";
import {
  MatButtonToggleGroup,
  MatButtonToggleModule,
} from "@angular/material/button-toggle";
import { MatTableModule } from "@angular/material/table";
import {
  MatPaginator,
  MatPaginatorModule,
  PageEvent,
} from "@angular/material/paginator";
import { MatCardModule } from "@angular/material/card";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";

import { FileService } from "../../services/file.service";
import { FileMetadata } from "../../models/metadata.model";
import { AuthService } from "../../../auth/services/auth.service";
import { ConfirmDialogService } from "../../../../shared/dialog/services/confirm-dialog.service";
import { firstValueFrom } from "rxjs";
import { MatDialog } from "@angular/material/dialog";
import { AlertDialog } from "../../../../shared/dialog/components/alert-dialog/alert-dialog";
import { createDialogForSuccessMessage } from "../../../../shared/dialog/models/alert-dialog.factory";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: "app-files",
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: "./files.html",
  styleUrl: "./files.scss",
})
export class Files implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);
  private readonly router: Router = new Router();
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly alerDialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  // AuthService signals
  readonly currentUser = this.authService.user;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly message = this.fileService.message;
  readonly error = this.fileService.errorStatus;

  constructor() {
    if (!this.isAuthenticated()) {
      this.router.navigate(["/home"]);
    }
  }

  ngOnInit() {
    this.fileService
      .getMyFiles()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((files) => {
        this.files.set(files);
      });
  }

  files = signal<FileMetadata[]>([]);
  filter: string = "active";

  // Pagination
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild("filterGroup") filterGroup!: MatButtonToggleGroup;
  pageSize = 5;
  pageSizeOptions = [3, 5];
  currentPageIndex = 0;

  // Computed réactifs
  get filteredFiles(): FileMetadata[] {
    const files = this.files();
    switch (this.filter) {
      case "active":
        return files.filter((f) => this.isActive(f));
      case "expired":
        return files.filter((f) => !this.isActive(f));
      default:
        return files;
    }
  }

  setFilter(newFilter: string): void {
    this.filter = newFilter;
    this.currentPageIndex = 0;
    if (this.paginator) this.paginator.firstPage();
  }

  get paginatedFiles(): FileMetadata[] {
    const start = this.currentPageIndex * this.pageSize;
    return this.filteredFiles.slice(start, start + this.pageSize);
  }

  onPageChange(event: PageEvent): void {
    this.currentPageIndex = event.pageIndex;
    this.pageSize = event.pageSize!;
  }

  isActive(file: FileMetadata): boolean {
    return new Date(file.expiresAt) > new Date();
  }

  onFilterChange(): void {
    this.currentPageIndex = 0; // Reset à première page
    if (this.paginator) {
      this.paginator.firstPage();
    }
  }

  getFileIcon(file: FileMetadata): string {
    if (file.contentType.startsWith("image/")) return "image";
    if (file.contentType.startsWith("video/")) return "movie";
    if (file.contentType.startsWith("audio/")) return "audiotrack";
    if (file.contentType.includes("pdf")) return "picture_as_pdf";

    return "insert_drive_file";
  }

  async delete(file: FileMetadata) {
    const confirmed = await this.confirmDialog.confirm({
      title: "Supprimer le fichier",
      message: `Voulez-vous vraiment supprimer "${file.filename}" ?`,
      confirmLabel: "Supprimer",
      cancelLabel: "Annuler",
    });

    if (!confirmed) return;

    console.log("delete", file);

    this.fileService
      .deleteMyFile(file.downloadToken)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.alerDialog.open(AlertDialog, {
            panelClass: "rounded-dialog",
            data: createDialogForSuccessMessage(
              this.message() || "Fichier supprimé avec succès",
            ),
          });
          this.fileService
            .getMyFiles()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((files) => {
              this.files.set(files);
            });
        },
        error: (err: HttpErrorResponse) => {
          console.error("Delete error: ", err);
          this.alerDialog.open(AlertDialog, {
            panelClass: "rounded-dialog",
            data: {
              title: "Erreur",
              message: this.message() || "Erreur lors de la suppression",
            },
          });
          this.message.set(null);
        },
      });
  }

  formatSize(bytes: number): string {
    if (bytes === 0) return "0 B";

    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
  }

  humanizeExpiresAt(file: FileMetadata): string {
    const target = new Date(file.expiresAt);
    const now = new Date();
    const diffMs = target.getTime() - now.getTime();
    const seconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);

    if (seconds < 1) {
      return "Expiré";
    }

    if (seconds < 60) {
      return "Expire dans quelques secondes";
    }
    if (minutes < 60) {
      return `Expire dans ${minutes} minute${minutes > 1 ? "s" : ""}`;
    }
    if (hours < 24) {
      return `Expire dans ${hours} heure${hours > 1 ? "s" : ""}`;
    }
    if (days < 7) {
      return `Expire dans ${days} jour${days > 1 ? "s" : ""}`;
    }
    return `Expire dans ${weeks} semaine${weeks > 1 ? "s" : ""}`;
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

  download(file: FileMetadata) {
    this.fileService
      .getPresignedDownloadUrl(file.downloadToken)
      .subscribe((presignedDownload) => {
        window.open(presignedDownload.downloadUrl, "_blank");
      });
  }
}
