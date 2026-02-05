// src/test/unit/shared/alert-dialog.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach } from "vitest";
// Angular Material
import { MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MAT_DIALOG_DATA } from "@angular/material/dialog";
import { AlertDialogData } from "../../../app/core/models/alert-dialog.model";
// SUT (System Under Test)
import { AlertDialog } from "../../../app/shared/alert-dialog/alert-dialog";

/** AlertDialog unit tests */
describe("AlertDialog (unit)", () => {
  const mockDialogDataWithErrors: AlertDialogData = {
    title: "Test Title",
    message: "Test Message",
    errors: {
      email: "Invalid email",
      password: "Too short",
    },
    path: "/auth",
    status: 400,
    timestamp: "2024-01-01T00:00:00Z",
  };

  const mockDialogDataNoErrors: AlertDialogData = {
    title: "No Errors",
    message: "No errors present",
    errors: undefined,
  };

  /** With errors */
  describe("With errors", () => {
    let component: AlertDialog;
    let fixture: ComponentFixture<AlertDialog>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [AlertDialog, MatDialogModule, MatButtonModule],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockDialogDataWithErrors },
        ],
      });

      fixture = TestBed.createComponent(AlertDialog);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it("should create", () => {
      expect(component).toBeTruthy();
    });

    it("should return error entries when errors exist", () => {
      const result = component.errorsEntries();

      expect(result).toHaveLength(2);
      expect(result[0]).toEqual({ key: "email", value: "Invalid email" });
      expect(result[1]).toEqual({ key: "password", value: "Too short" });
    });
  });

  //** Without errors */
  describe("Without errors", () => {
    let component: AlertDialog;
    let fixture: ComponentFixture<AlertDialog>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [AlertDialog, MatDialogModule, MatButtonModule],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockDialogDataNoErrors },
        ],
      });

      fixture = TestBed.createComponent(AlertDialog);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it("should return empty array when no errors", () => {
      const result = component.errorsEntries();
      expect(result).toEqual([]);
    });
  });
});
