import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { ConfirmDialogData } from "../../models/confirm-dialog.model";

@Component({
  selector: "app-confirm-dialog",
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: "./confirm-dialog.html",
  styleUrl: "./confirm-dialog.scss",
})
export class ConfirmDialog {
  readonly data = inject(MAT_DIALOG_DATA) as ConfirmDialogData;
  private dialogRef = inject(MatDialogRef<ConfirmDialog>);

  onConfirm() {
    this.dialogRef.close(true);
  }

  onCancel() {
    this.dialogRef.close(false);
  }
}
