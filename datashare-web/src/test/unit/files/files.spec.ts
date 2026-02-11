// src/test/unit/files/files.spec.ts

// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { of, throwError } from "rxjs";
import { signal } from "@angular/core";
import {
  provideRouter,
  Router,
  withComponentInputBinding,
} from "@angular/router";
// SUT (System Under Test)
import { Files } from "../../../app/features/files/components/files/files";
import { FileMetadata } from "../../../app/features/files/models/metadata.model";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { AlertDialog } from "../../../app/shared/dialog/components/alert-dialog/alert-dialog";
import { ConfirmDialogService } from "../../../app/shared/dialog/services/confirm-dialog.service";

const mockFiles: FileMetadata[] = [
  {
    filename: "test-file.pdf",
    contentType: "text/pdf",
    size: 1024,
    downloadToken: "ABC",
    createdAt: new Date().toISOString(),
    expiresAt: new Date().toISOString(),
  },
];

const mockPresignedUrl = {
  downloadUrl: "https://presigned-url.com/file.pdf",
};

/** Files Component Unit tests */
describe("Files (unit)", () => {
  let component: Files;
  let fixture: ComponentFixture<Files>;
  let mockFileService: any;
  let mockAuthService: any;
  let mockRouter: any;
  let mockConfirmDialog: any;
  let mockAlertDialog: any;

  beforeEach(async () => {
    const mockDialogRef = {
      close: vi.fn(),
    };

    mockConfirmDialog = {
      confirm: vi.fn(),
    };

    mockAlertDialog = {
      open: vi.fn().mockReturnValue(mockDialogRef),
    };

    mockAuthService = {
      user: signal<any>(null),
      isAuthenticated: signal<boolean>(true),
      message: signal<string>(""),
    };

    mockFileService = {
      getMyFiles: vi.fn(() => of([])),
      getPresignedDownloadUrl: vi.fn(() => of(mockPresignedUrl)),
      message: vi.fn(() => null),
      deleteMyFile: vi.fn(() => of(null)),
    };

    mockRouter = vi.fn();

    await TestBed.configureTestingModule({
      imports: [Files],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        { provide: "AlertDialog", useValue: mockAlertDialog },
        { provide: ConfirmDialogService, useValue: mockConfirmDialog },
        provideRouter([], withComponentInputBinding()),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Files);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  describe("ngOnInit", () => {
    it("should load files on init", () => {
      mockFileService.getMyFiles.mockReturnValue(of(mockFiles));

      component.ngOnInit();

      expect(mockFileService.getMyFiles).toHaveBeenCalled();
      expect(component.files()).toEqual(mockFiles);
    });
  });

  describe("download", () => {
    it("should call download service with correct token", () => {
      mockFileService.getPresignedDownloadUrl.mockReturnValue(
        of(mockPresignedUrl),
      );

      component.download(mockFiles[0]);

      expect(mockFileService.getPresignedDownloadUrl).toHaveBeenCalledWith(
        mockFiles[0].downloadToken,
      );
    });
  });

  describe("Signals", () => {
    it("should expose auth service signals correctly", () => {
      expect(component.currentUser()).toBeNull();
      expect(component.isAuthenticated()).toBe(true);
      expect(component.message()).toBeNull;
      expect(component.files()).toEqual([]);
    });

    it("should reflect auth service signal changes", () => {
      const mockUser = { name: "test" };
      mockAuthService.user.set(mockUser);

      expect(component.currentUser()).toEqual(mockUser);
    });
  });

  describe("delete(file)", () => {
    it("should NOT call API if confirmation cancelled", async () => {
      // GIVEN confirm retourne false
      mockConfirmDialog.confirm.mockResolvedValue(false);

      // WHEN delete appelé
      await component.delete(mockFiles[0]);

      // THEN pas d'appel API
      expect(mockFileService.deleteMyFile).not.toHaveBeenCalled();
      expect(mockAlertDialog.open).not.toHaveBeenCalled();
    });

    it("should delete file successfully and refresh list", async () => {
      // GIVEN confirm OK + API success
      mockConfirmDialog.confirm.mockResolvedValue(true);
      mockFileService.deleteMyFile.mockReturnValue(of(null));
      mockFileService.getMyFiles.mockReturnValue(of([]));

      // WHEN delete appelé
      await component.delete(mockFiles[0]);

      // THEN
      expect(mockConfirmDialog.confirm).toHaveBeenCalledWith({
        title: "Supprimer le fichier",
        message: `Voulez-vous vraiment supprimer "${mockFiles[0].filename}" ?`,
        confirmLabel: "Supprimer",
        cancelLabel: "Annuler",
      });
      expect(mockFileService.deleteMyFile).toHaveBeenCalledWith(
        mockFiles[0].downloadToken,
      );
      expect(mockFileService.getMyFiles).toHaveBeenCalled();
    });

    it("should show correct dialog message with filename", async () => {
      const longFilename = "very-long-filename-document-123.pdf";
      const fileWithLongName: FileMetadata = {
        ...mockFiles[0],
        filename: longFilename,
      };

      mockConfirmDialog.confirm.mockResolvedValue(true);

      await component.delete(fileWithLongName);

      expect(mockConfirmDialog.confirm).toHaveBeenCalledWith({
        title: "Supprimer le fichier",
        message: expect.stringContaining(longFilename),
        confirmLabel: "Supprimer",
        cancelLabel: "Annuler",
      });
    });
  });

  describe("delete() - Edge Cases", () => {
    it("should handle confirmDialog throwing error", async () => {
      mockConfirmDialog.confirm.mockRejectedValue(new Error("Dialog error"));

      await expect(component.delete(mockFiles[0])).rejects.toThrow(
        "Dialog error",
      );
      expect(mockFileService.deleteMyFile).not.toHaveBeenCalled();
    });

    it("should log file info before delete", async () => {
      const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});
      mockConfirmDialog.confirm.mockResolvedValue(true);

      await component.delete(mockFiles[0]);

      expect(consoleSpy).toHaveBeenCalledWith("delete", mockFiles[0]);
      consoleSpy.mockRestore();
    });
  });
});
