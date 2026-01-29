import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { AlertDialogData } from '../../core/models/alert-dialog.model';

@Component({
  selector: 'app-alert-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './alert-dialog.html',
  styleUrl: './alert-dialog.scss',
})
export class AlertDialog {
  readonly data = inject(MAT_DIALOG_DATA) as AlertDialogData;

  validationErrorEntries() {
    return this.data.validationErrors
      ? Object.entries(this.data.validationErrors).map((key, value) => ({ key, value }))
      : [];
  }
}
