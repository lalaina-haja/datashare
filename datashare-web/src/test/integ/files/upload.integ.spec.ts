import { ComponentFixture, TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";
import { provideRouter, withComponentInputBinding } from "@angular/router";
import { of } from "rxjs";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { HttpEventType } from "@angular/common/http";

import { PresignedUpload } from "../../../app/features/files/models/presigned-upload.model";
import { Upload } from "../../../app/features/files/components/upload/upload";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { ConfigService } from "../../../app/core/services/config.service";

// Mock données
const mockFile = new File(["test content"], "document.pdf", {
  type: "application/pdf",
});

const mockPresignedUpload: PresignedUpload = {
  uploadUrl: "https://s3.amazonaws.com/bucket/test",
  tokenString: "upload-token-123",
  expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
};

describe("Upload (integ)", () => {
  let fixture: ComponentFixture<Upload>;
  let mockFileService: any;
  let mockAuthService: any;
  let mockConfigService: any;

  beforeEach(async () => {
    mockAuthService = {
      isAuthenticated: vi.fn(() => true),
      user: { value: null },
    };

    mockFileService = {
      clearMessage: vi.fn(),
      message: vi.fn(() => ""),
      errorStatus: vi.fn(() => ""),
      getPresignedUploadUrl: vi.fn(() => of(mockPresignedUpload)),
      uploadToS3: vi.fn(() => of({ type: HttpEventType.Response })),
    };

    mockConfigService = {
      webBaseUrl: "https://app.example.com",
    };

    await TestBed.configureTestingModule({
      imports: [Upload],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ConfigService, useValue: mockConfigService },
        provideRouter(
          [{ path: "**", component: Upload }],
          withComponentInputBinding(),
        ),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Upload);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should render upload interface", () => {
    fixture.detectChanges();

    const title = fixture.debugElement.nativeElement.textContent;
    expect(title).toContain("Ajouter");
    expect(
      fixture.debugElement.query(By.css('input[type="file"]')),
    ).toBeTruthy();
    expect(fixture.debugElement.query(By.css("button"))).toBeTruthy();
  });

  it("should trigger hidden file input on click", async () => {
    fixture.detectChanges();

    const triggerBtn = fixture.debugElement.query(
      By.css('button[class*="btn-charger"]'),
    );
    const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
    const clickSpy = vi.spyOn(fileInput, "click");

    triggerBtn?.nativeElement.click();

    expect(clickSpy).toHaveBeenCalledTimes(1);
  });

  it("should set selected file and reset state", async () => {
    fixture.detectChanges();

    const mockEvent = {
      target: {
        files: [mockFile],
      },
    } as any;

    const fileInput = fixture.debugElement.query(By.css('input[type="file"]'));
    fileInput.triggerEventHandler("change", mockEvent);

    await new Promise((resolve) => setTimeout(resolve, 0));
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component.file()).toBe(mockFile);
    expect(component.progress()).toBeNull();
    expect(mockFileService.clearMessage).toHaveBeenCalledTimes(1);
  });

  it("should complete full upload flow", async () => {
    // 1. Sélection fichier
    fixture.detectChanges();
    const fileInput = fixture.debugElement.query(By.css('input[type="file"]'));
    const mockFileEvent = { target: { files: [mockFile] } } as any;
    fileInput.triggerEventHandler("change", mockFileEvent);

    // 2. Force async resolution
    await new Promise((resolve) => setTimeout(resolve, 0));
    fixture.detectChanges();

    // 3. Upload
    const uploadBtn = fixture.debugElement.query(
      By.css("button.btn-televerser"),
    );
    uploadBtn.nativeElement.click();

    // 4. Attendre RxJS + détection changements
    await new Promise((resolve) => setTimeout(resolve, 50));
    fixture.detectChanges();

    // Vérifications
    expect(mockFileService.getPresignedUploadUrl).toHaveBeenCalledWith(
      mockFile,
      7,
    );
    expect(mockFileService.uploadToS3).toHaveBeenCalledWith(
      mockPresignedUpload.uploadUrl,
      mockFile,
    );
    expect(fixture.componentInstance.fileDownloadUrl()).toEqual(
      "https://app.example.com/d/upload-token-123",
    );
  });

  it("should show correct file icons", () => {
    fixture.detectChanges();

    const testCases = [
      { type: "application/pdf", expected: "picture_as_pdf" },
      { type: "image/jpeg", expected: "image" },
      { type: "video/mp4", expected: "movie" },
      { type: undefined, expected: "insert_drive_file" },
    ];

    testCases.forEach(({ type, expected }) => {
      const testFile = new File(["test"], "test", { type });
      const mockEvent = {
        target: { files: [testFile] },
      } as any;
      const fileInput = fixture.debugElement.query(
        By.css('input[type="file"]'),
      );
      fileInput.triggerEventHandler("change", mockEvent);
      expect(fixture.componentInstance.getFileIcon()).toBe(expected);
    });
  });

  it("should disable upload button when no file", () => {
    fixture.detectChanges();

    const uploadBtn = fixture.debugElement.query(
      By.css('button[class*="btn-televerser"]'),
    );
    expect(uploadBtn.nativeElement.disabled).toBeTruthy();
  });

  it("should redirect if not authenticated", () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);

    const newFixture = TestBed.createComponent(Upload);
    newFixture.detectChanges();

    expect(mockAuthService.isAuthenticated).toHaveBeenCalled();
  });

  it("should humanize dates correctly (zoneless)", () => {
    const component = fixture.componentInstance;

    // Vitest fake timers (zoneless compatible)
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-02-10T16:00:00Z"));

    expect(component.humanize("2026-02-10T16:00:30Z")).toBe(
      "pendant quelques secondes",
    );
    expect(component.humanize("2026-02-10T16:02:30Z")).toBe(
      "pendant 2 minutes",
    );

    vi.useRealTimers();
  });
});
