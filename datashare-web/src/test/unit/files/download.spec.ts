import { ComponentFixture, TestBed } from "@angular/core/testing";
import { of, throwError } from "rxjs";
import { vi, describe, it, expect, beforeEach, afterEach } from "vitest";
import { HttpErrorResponse } from "@angular/common/http";
import { provideZonelessChangeDetection } from "@angular/core";

import { ActivatedRoute, Router } from "@angular/router";
import { PresignedDownload } from "../../../app/features/files/models/presigned-download.model";
import { Download } from "../../../app/features/files/components/download/download";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";

// Mock data
const mockToken = "download-token-123";
const mockPresignedDownload: PresignedDownload = {
  downloadUrl: "https://s3.amazonaws.com/bucket/file.pdf",
  contentType: "application/pdf",
  filename: "document.pdf",
  size: 1250000, // 1.25 MB
  createdAt: new Date(Date.now()).toISOString(),
  expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
};

describe("Download - Unit Tests (Zoneless Angular 21)", () => {
  let fixture: ComponentFixture<Download>;
  let mockFileService: any;
  let mockAuthService: any;
  let mockRouter: any;
  let mockRoute: any;

  beforeEach(async () => {
    mockAuthService = {
      isAuthenticated: vi.fn(() => true),
    };

    mockFileService = {
      clearMessage: vi.fn(),
      message: vi.fn(() => ""),
      errorStatus: vi.fn(() => false),
      getPresignedDownloadUrl: vi.fn(),
    };

    mockRouter = {
      navigate: vi.fn(),
    };

    const mockParamMap = {
      get: vi.fn((key: string) => mockToken),
    };
    mockRoute = {
      paramMap: of(mockParamMap),
    };

    await TestBed.configureTestingModule({
      imports: [Download],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideZonelessChangeDetection(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Download);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should create", () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  describe("ngOnInit", () => {
    it("should load file data from route param token", async () => {
      mockFileService.getPresignedDownloadUrl.mockReturnValue(
        of(mockPresignedDownload),
      );

      fixture.detectChanges();
      await new Promise((r) => setTimeout(r, 0)); // Zoneless RxJS

      expect(mockFileService.clearMessage).toHaveBeenCalled();
      expect(mockFileService.getPresignedDownloadUrl).toHaveBeenCalledWith(
        mockToken,
      );
      expect(fixture.componentInstance.file()).toEqual(mockPresignedDownload);
    });

    it("should handle download API error", async () => {
      const mockError = new HttpErrorResponse({
        status: 404,
        statusText: "Not Found",
      });
      mockFileService.getPresignedDownloadUrl.mockReturnValue(
        throwError(() => mockError),
      );

      const consoleErrorSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      fixture.detectChanges();
      await new Promise((r) => setTimeout(r, 0));

      expect(fixture.componentInstance.file()).toBeNull();
      expect(consoleErrorSpy).toHaveBeenCalled();
      consoleErrorSpy.mockRestore();
    });
  });

  describe("clearSignals", () => {
    it("should clear file service message and reset file signal", () => {
      fixture.componentInstance.file.set(mockPresignedDownload);

      fixture.componentInstance.clearSignals();

      expect(mockFileService.clearMessage).toHaveBeenCalledTimes(1);
      expect(fixture.componentInstance.file()).toBeNull();
    });
  });

  describe("getFileIcon", () => {
    it.each([
      { contentType: "image/jpeg", expected: "image" },
      { contentType: "video/mp4", expected: "movie" },
      { contentType: "audio/mpeg", expected: "audiotrack" },
      { contentType: "application/pdf", expected: "picture_as_pdf" },
      { contentType: "text/plain", expected: "insert_drive_file" },
    ])(
      'should return "$expected" icon for contentType "$contentType"',
      ({ contentType, expected }) => {
        const testFile: PresignedDownload = {
          ...mockPresignedDownload,
          contentType,
        };
        fixture.componentInstance.file.set(testFile);

        expect(fixture.componentInstance.getFileIcon()).toBe(expected);
      },
    );
  });

  describe("download", () => {
    it("should open download URL in new tab", () => {
      const windowOpenSpy = vi
        .spyOn(window, "open")
        .mockImplementation(() => null as any);

      fixture.componentInstance.file.set(mockPresignedDownload);
      fixture.componentInstance.download();

      expect(windowOpenSpy).toHaveBeenCalledWith(
        mockPresignedDownload.downloadUrl,
        "_blank",
      );
      expect(windowOpenSpy).toHaveBeenCalledTimes(1);
      vi.restoreAllMocks();
    });
  });

  describe("isActive", () => {
    it("should return true for non-expired file", () => {
      const activeFile: PresignedDownload = {
        ...mockPresignedDownload,
        expiresAt: new Date(Date.now() + 86400000).toISOString(), // +1 day
      };

      expect(fixture.componentInstance.isActive(activeFile)).toBe(true);
    });

    it("should return false for expired file", () => {
      const expiredFile: PresignedDownload = {
        ...mockPresignedDownload,
        expiresAt: new Date(Date.now() - 86400000).toISOString(), // -1 day
      };

      expect(fixture.componentInstance.isActive(expiredFile)).toBe(false);
    });
  });

  describe("formatSize", () => {
    it.each([
      { bytes: 0, expected: "0 B" },
      { bytes: 1500, expected: "1.5 KB" },
      { bytes: 1250000, expected: "1.2 MB" },
      { bytes: 1500000000, expected: "1.4 GB" },
    ])('should format $bytes bytes as "$expected"', ({ bytes, expected }) => {
      expect(fixture.componentInstance.formatSize(bytes)).toBe(expected);
    });
  });

  describe("messageExpiration", () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date("2026-02-11T14:00:00Z"));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it.each([
      // Expired
      {
        expiresAt: new Date("2026-02-11T13:59:59Z"),
        expected:
          "Ce fichier n'est plus disponible en téléchargement car il a expiré",
      },
      // Seconds
      {
        expiresAt: new Date("2026-02-11T14:00:30Z"),
        expected: "Ce fichier expirera dans quelques secondes",
      },
      // Minutes
      {
        expiresAt: new Date("2026-02-11T14:02:00Z"),
        expected: "Ce fichier expirera dans 2 minutes",
      },
      // Hours
      {
        expiresAt: new Date("2026-02-11T16:00:00Z"),
        expected: "Ce fichier expirera dans 2 heures",
      },
      // Tomorrow
      {
        expiresAt: new Date("2026-02-12T14:00:00Z"),
        expected: "Ce fichier expirera demain",
      },
      // Days
      {
        expiresAt: new Date("2026-02-13T14:00:00Z"),
        expected: "Ce fichier expirera dans 2 jours",
      },
    ])(
      'should return "$expected" for expiration',
      ({ expiresAt, expected }) => {
        const testFile: PresignedDownload = {
          ...mockPresignedDownload,
          expiresAt: expiresAt.toISOString(),
        };

        expect(fixture.componentInstance.messageExpiration(testFile)).toBe(
          expected,
        );
      },
    );
  });

  describe("truncateFileName", () => {
    it.each([
      { filename: "short.txt", maxLength: 10, expected: "short.txt" },
      {
        filename: "very_long_filename.pdf",
        maxLength: 14,
        expected: "very_lo....pdf",
      },
      { filename: "document.pdf", maxLength: 15, expected: "document.pdf" },
      { filename: "noextension", maxLength: 8, expected: "noextens..." },
    ])(
      'should truncate "$filename" to "$expected" (maxLength: $maxLength)',
      ({ filename, maxLength, expected }) => {
        expect(
          fixture.componentInstance.truncateFileName(filename, maxLength),
        ).toBe(expected);
      },
    );
  });
});
