import { ComponentFixture, TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";
import { provideRouter, Router } from "@angular/router";
import { of, throwError } from "rxjs";
import { vi, describe, it, expect, beforeEach, afterEach } from "vitest";
import { HttpErrorResponse } from "@angular/common/http";
import { provideZonelessChangeDetection, signal } from "@angular/core";
import { PresignedDownload } from "../../../app/features/files/models/presigned-download.model";
import { Download } from "../../../app/features/files/components/download/download";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";

const mockToken = "download-token-123";
const mockPresignedDownload: PresignedDownload = {
  downloadUrl: "https://s3.amazonaws.com/bucket/document.pdf",
  contentType: "application/pdf",
  filename: "document.pdf",
  size: 1250000,
  createdAt: new Date(Date.now()).toISOString(),
  expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
};

describe("Download - INTEGRATION Tests (DOM + Router + Services)", () => {
  let fixture: ComponentFixture<Download>;
  let mockFileService: any;
  let mockAuthService: any;
  let windowOpenSpy: any;

  beforeEach(async () => {
    mockAuthService = {
      isAuthenticated: vi.fn(() => true),
    };

    mockFileService = {
      clearMessage: vi.fn(),
      message: vi.fn(() => ""),
      errorStatus: signal(false),
      getPresignedDownloadUrl: vi.fn(() => of(mockPresignedDownload)),
    };

    windowOpenSpy = vi.spyOn(window, "open").mockImplementation(() => null);

    await TestBed.configureTestingModule({
      imports: [Download],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        provideRouter([
          { path: "download/:token", component: Download },
          { path: "**", redirectTo: "/home" },
        ]),
        provideZonelessChangeDetection(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Download);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe("DOM Rendering + Router Integration", () => {
    it("should render title and file info when token valid", async () => {
      // Navigue vers /download/{token}
      const router = TestBed.inject(Router);
      await router.navigate(["/download", mockToken]);

      await new Promise((r) => setTimeout(r, 100)); // Router + RxJS
      fixture.detectChanges();

      // DOM vérifications
      expect(fixture.nativeElement.textContent).toContain("document.pdf");
      expect(fixture.nativeElement.textContent).toContain("1.2 MB");
      expect(
        fixture.debugElement.query(By.css("mat-icon"))?.nativeElement
          .textContent,
      ).toBe("picture_as_pdf");
    });
  });

  describe("Download Button + window.open", () => {
    it("should trigger download when button clicked", async () => {
      await TestBed.inject(Router).navigate(["/download", mockToken]);
      await new Promise((r) => setTimeout(r, 100));
      fixture.detectChanges();

      const downloadBtn = fixture.debugElement.query(
        By.css('button[aria-label*="télécharger"]'),
      );
      downloadBtn?.nativeElement.click();

      expect(windowOpenSpy).toHaveBeenCalledWith(
        mockPresignedDownload.downloadUrl,
        "_blank",
      );
    });

    it("should disable download if file expired", async () => {
      const expiredFile: PresignedDownload = {
        ...mockPresignedDownload,
        expiresAt: new Date(Date.now() - 86400000).toISOString(),
      };
      mockFileService.getPresignedDownloadUrl.mockReturnValue(of(expiredFile));

      await TestBed.inject(Router).navigate(["/download", mockToken]);
      await new Promise((r) => setTimeout(r, 100));
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        "Ce fichier n'est plus disponible",
      );
    });
  });

  describe("Utility Functions DOM Integration", () => {
    beforeEach(async () => {
      await TestBed.inject(Router).navigate(["/download", mockToken]);
      await new Promise((r) => setTimeout(r, 100));
      fixture.detectChanges();
    });

    it("should display truncated filename", () => {
      const longFilename = "very_long_filename_with_extension_123456789.pdf";
      const truncated = fixture.componentInstance.truncateFileName(
        longFilename,
        15,
      );
      expect(truncated).toBe("very_lon....pdf");
      expect(fixture.nativeElement.textContent).toContain("document.pdf");
    });

    it("should show formatted file size", () => {
      expect(fixture.nativeElement.textContent).toContain("1.2 MB");
    });

    it("should display expiration message", () => {
      const expirationText = fixture.componentInstance.messageExpiration(
        mockPresignedDownload,
      );
      expect(expirationText).toContain("expirera");
      expect(fixture.nativeElement.textContent).toContain("expirera");
    });
  });
});
