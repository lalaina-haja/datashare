import { Injectable, inject } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { ConfirmDialogData } from "../models/confirm-dialog.model";
import { ConfirmDialog } from "../components/confirm-dialog/confirm-dialog";
import { firstValueFrom } from "rxjs";

@Injectable({ providedIn: "root" })
export class ConfirmDialogService {
  private dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData) {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: "400px",
      data,
      panelClass: "rounded-dialog",
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
