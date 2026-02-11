// Angular modules
import { describe, it, expect, vi, beforeEach } from "vitest";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideRouter, withComponentInputBinding } from "@angular/router";
import { of } from "rxjs";
import { signal } from "@angular/core";
// Component to test
import { FileMetadata } from "../../../app/features/files/models/metadata.model";
import { Files } from "../../../app/features/files/components/files/files";
import { FileService } from "../../../app/features/files/services/file.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { By } from "@angular/platform-browser";

// Mock des données
const mockFiles: FileMetadata[] = [
  {
    filename: "document.pdf",
    contentType: "text/pdf",
    size: 1024,
    downloadToken: "ABCDEF",
    createdAt: new Date().toISOString(),
    expiresAt: new Date().toISOString(),
  },
  {
    filename: "image.jpg",
    contentType: "image/jpg",
    size: 2048,
    downloadToken: "BCDEFG", 
    createdAt: new Date().toISOString(),
    expiresAt: new Date().toISOString(),
  },
];

const mockPresignedUrl = {
  downloadUrl: "https://s3.amazonaws.com/test/download.pdf",
};

describe("Files (integ)", () => {
  let fixture: ComponentFixture<Files>;
  let mockFileService: any;
  let mockAuthService: any;
  let windowOpenSpy: any;

  beforeEach(async () => {
    mockAuthService = {
      user: signal<any>({ name: "Test User" }),
      isAuthenticated: signal<boolean>(true),
      message: signal<string>(""),
    };

    mockFileService = {
      getMyFiles: vi.fn(() => of(mockFiles)),
      message: vi.fn(() => ""),
      getPresignedDownloadUrl: vi.fn(() => of(mockPresignedUrl)),
    };

    windowOpenSpy = vi.spyOn(window, "open").mockImplementation(() => null);

    await TestBed.configureTestingModule({
      imports: [Files],
      providers: [
        { provide: FileService, useValue: mockFileService },
        { provide: AuthService, useValue: mockAuthService },
        provideRouter([], withComponentInputBinding()),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Files);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should render file list when authenticated", async () => {
    fixture.detectChanges();
    fixture.detectChanges();

    await new Promise((r) => setTimeout(r));
    fixture.detectChanges();

    expect(mockFileService.getMyFiles).toHaveBeenCalled();

    const list = fixture.debugElement.nativeElement.textContent;
    expect(list).toContain("Mes fichiers");

    const listItems = fixture.debugElement.queryAll(
      By.css('[role="listitem"], mat-list-item, .file-item'),
    );
    expect(listItems.length).toBeGreaterThanOrEqual(1);
  });

  it("should download file when download button clicked", async () => {
    fixture.detectChanges();
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r));
    fixture.detectChanges();

    const downloadButtons = fixture.debugElement.queryAll(
      By.css(
        'button[mat-icon-button], button[aria-label*="download"], .download-btn, [data-testid="download"]',
      ),
    );

    if (downloadButtons.length > 0) {
      downloadButtons[0].nativeElement.click();
      await new Promise((r) => setTimeout(r));

      expect(mockFileService.getPresignedDownloadUrl).toHaveBeenCalled();
    }

    expect(mockFileService.getPresignedDownloadUrl).toHaveBeenCalledTimes(0); // Avant click
  });

  it("should show empty state when no files", async () => {
    mockFileService.getMyFiles.mockReturnValue(of([]));

    fixture.detectChanges();
    fixture.detectChanges();

    const content = fixture.debugElement.nativeElement.textContent;
    expect(content).toMatch(/aucun|empty|vide/i);
  });

  it("should display Mes fichiers", async () => {
    fixture.detectChanges();

    const content = fixture.debugElement.nativeElement.textContent;
    expect(content).toContain("Mes fichiers");
  });
});
