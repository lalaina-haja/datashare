// Angular Testing
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { describe, it, expect, beforeEach, vi } from "vitest";
import { ReactiveFormsModule } from "@angular/forms";
import {
  provideRouter,
  withComponentInputBinding,
  Router,
} from "@angular/router";
import { of } from "rxjs";
import { HttpEventType } from "@angular/common/http";
// SUT (System Under Test)
import { PresignedUpload } from "../../../app/features/files/models/presigned-upload.model";
import { Upload } from "../../../app/features/files/components/upload/upload";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { ConfigService } from "../../../app/core/services/config.service";
import { signal } from "@angular/core";

// Mock des données
const mockPresignedUpload: PresignedUpload = {
  uploadUrl: "https://s3.amazonaws.com/test-bucket/upload",
  tokenString: "upload-token-123",
  expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
};

const mockFile: File = new File(["content"], "test.pdf", {
  type: "application/pdf",
});

describe("Upload", () => {
  let component: Upload;
  let fixture: ComponentFixture<Upload>;
  let mockFileService: any;
  let mockAuthService: any;
  let mockConfigService: any;
  let mockRouter: any;

  beforeEach(async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-02-10T10:00:00Z"));

    mockAuthService = {
      isAuthenticated: vi.fn(() => true),
    };

    mockFileService = {
      clearMessage: vi.fn(),
      message: signal<string>(""),
      errorStatus: signal<boolean>(false),
      getPresignedUploadUrl: vi.fn(),
      uploadToS3: vi.fn(),
    };

    mockConfigService = {
      webBaseUrl: "https://app.example.com",
    };

    mockRouter = vi.fn();

    await TestBed.configureTestingModule({
      imports: [Upload, ReactiveFormsModule],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ConfigService, useValue: mockConfigService },
        { provide: Router, useValue: mockRouter },
        provideRouter([], withComponentInputBinding()),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Upload);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  describe("ngOnInit", () => {
    it("should initialize correctly when authenticated", () => {
      component.ngOnInit();

      expect(mockFileService.clearMessage).toHaveBeenCalled();
      expect(component.fileDownloadUrl()).toBeNull();
      expect(component.fileExpiration()).toBeNull();
    });
  });

  describe("Signals", () => {
    it("should expose signals correctly", () => {
      expect(component.file()).toBeNull();
      expect(component.progress()).toBeNull();
      expect(component.expirationDays()).toBe(7);
      expect(component.uploadSuccess()).toBe(false);
    });

    it("should compute uploadSuccess based on fileDownloadUrl", () => {
      component.fileDownloadUrl.set("https://example.com");
      expect(component.uploadSuccess()).toBe(true);

      component.fileDownloadUrl.set(null);
      expect(component.uploadSuccess()).toBe(false);
    });
  });

  describe("triggerFileSelect", () => {
    it("should trigger click on input element", () => {
      const mockInput = { click: vi.fn() } as any;

      component.triggerFileSelect(mockInput);

      expect(mockInput.click).toHaveBeenCalled();
    });
  });

  describe("onFileSelected", () => {
    it("should set file signal when file selected", () => {
      const mockEvent = {
        target: {
          files: [mockFile],
        },
      } as any;

      component.onFileSelected(mockEvent);

      expect(component.file()).toEqual(mockFile);
      expect(component.progress()).toBeNull();
      expect(mockFileService.clearMessage).toHaveBeenCalled();
    });

    it("should not set file when no files selected", () => {
      const mockEvent = {
        target: {
          files: [],
        },
      } as any;

      const initialFile = component.file();

      component.onFileSelected(mockEvent);

      expect(component.file()).toBeNull();
    });
  });

  describe("upload", () => {
    it("should not upload when no file selected", () => {
      mockFileService.getPresignedUploadUrl.mockReturnValue(
        of(mockPresignedUpload),
      );

      component.upload();

      expect(mockFileService.getPresignedUploadUrl).not.toHaveBeenCalled();
    });

    it("should start upload flow with file", () => {
      mockFileService.getPresignedUploadUrl.mockReturnValue(
        of(mockPresignedUpload),
      );
      mockFileService.uploadToS3.mockReturnValue(
        of({ type: HttpEventType.Response }),
      );

      component.file.set(mockFile);
      component.expirationDays.set(7);

      component.upload();

      expect(mockFileService.clearMessage).toHaveBeenCalled();
      expect(mockFileService.getPresignedUploadUrl).toHaveBeenCalledWith(
        mockFile,
        7,
      );
      expect(mockFileService.uploadToS3).toHaveBeenCalledWith(
        mockPresignedUpload.uploadUrl,
        mockFile,
      );
      expect(component.fileDownloadUrl()).toEqual(
        "https://app.example.com/d/upload-token-123",
      );
    });
  });

  describe("getFileIcon", () => {
    it("should return default icon when no file", () => {
      expect(component.getFileIcon()).toBe("insert_drive_file");
    });

    it.each([
      ["image/png", "image"],
      ["video/mp4", "movie"],
      ["audio/mp3", "audiotrack"],
      ["application/pdf", "picture_as_pdf"],
    ])("should return %s icon for %s file", (fileType, expectedIcon) => {
      const testFile = new File([""], "test", { type: fileType });
      component.file.set(testFile);

      expect(component.getFileIcon()).toBe(expectedIcon);
    });
  });

  describe("humanize", () => {
    it.each([
      ["2026-02-10T10:00:30Z", "pendant quelques secondes"],
      ["2026-02-10T10:02:30Z", "pendant 2 minutes"],
      ["2026-02-10T12:02:30Z", "pendant 2 heures"],
      ["2026-02-12T10:02:30Z", "pendant 2 jours"],
      ["2026-03-10T10:02:30Z", "pendant 4 semaines"],
    ])('should humanize "%s" to "%s"', (dateString, expected) => {
      expect(component.humanize(dateString)).toBe(expected);
    });
  });
});
