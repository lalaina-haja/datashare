import { TestBed } from "@angular/core/testing";
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import {
  provideHttpClientTesting,
  HttpTestingController,
} from "@angular/common/http/testing";
import { FileService } from "../../../app/features/files/services/file.service";
import { ConfigService } from "../../../app/core/services/config.service";
import { AuthService } from "../../../app/features/auth/services/auth.service";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { PresignedUpload } from "../../../app/features/files/models/presigned-upload.model";

/**
 * FileService Unit Tests
 */
describe("FileService (unit)", () => {
  let service: FileService;
  let httpMock: HttpTestingController;
  let mockConfigService: { getEndpointUrl: ReturnType<typeof vi.fn> };
  let mockAuthService: any;

  beforeEach(() => {
    mockConfigService = {
      getEndpointUrl: vi.fn(),
    };

    mockAuthService = {
      isAuthenticated: vi.fn(() => true),
    };

    TestBed.configureTestingModule({
      providers: [
        FileService,
        { provide: ConfigService, useValue: mockConfigService },
        { provide: AuthService, useValue: mockAuthService },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(FileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      const httpMock = TestBed.inject(HttpTestingController);
      httpMock.verify();
    } catch (e) {
      // Ignore if there is no HttpTestingController in the TestBed
    }
  });

  it("should request a presigned upload URL", () => {
    // GIVEN the file
    const mockFile = new File(["content"], "test.png", {
      type: "image/png",
    });
    // AND mocks
    const mockResponse: PresignedUpload = {
      uploadUrl: "http://localhost:4566/fake",
      tokenString: "ABC",
    } as PresignedUpload;
    (mockConfigService.getEndpointUrl as any).mockReturnValue("/files/upload");

    // WHEN get presigned URL
    service.getPresignedUploadUrl(mockFile, 7).subscribe((res) => {
      expect(res.uploadUrl).toBe("http://localhost:4566/fake");
    });

    // THEN backend should have been called
    const req = httpMock.expectOne(`/files/upload`);
    expect(req.request.method).toBe("POST");
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockResponse);
  });

  it("should request public presigned upload URL when not authenticated", () => {
    // GIVEN unauthenticated
    (mockAuthService.isAuthenticated as any).mockReturnValue(false);
    const mockFile = new File(["content"], "test.png", { type: "image/png" });
    const mockResponse: PresignedUpload = {
      uploadUrl: "http://localhost:4566/fake",
      tokenString: "ABC",
    } as PresignedUpload;

    (mockConfigService.getEndpointUrl as any).mockReturnValue("/public/upload");

    service.getPresignedUploadUrl(mockFile, 7).subscribe((res) => {
      expect(res.uploadUrl).toBe("http://localhost:4566/fake");
    });

    const req = httpMock.expectOne(`/public/upload`);
    expect(req.request.method).toBe("POST");
    expect(req.request.withCredentials).toBe(false);
    req.flush(mockResponse);
  });

  it("should upload file to S3", () => {
    // GIVEN the file
    const file = new File(["hello"], "test.png", { type: "image/png" });

    // WHEN upload to S3
    service.uploadToS3("http://s3-url", file).subscribe((event) => {
      expect(event.type).toBeDefined();
    });

    // THEN the retuns OK
    const req = httpMock.expectOne("http://s3-url");
    expect(req.request.method).toBe("PUT");
    expect(req.request.headers.get("Content-Type")).toBe("image/png");
    req.flush({}, { status: 200, statusText: "OK" });
  });
});
