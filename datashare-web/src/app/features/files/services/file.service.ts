// src/app/features/files/services/file.service.ts

import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { catchError, Observable, tap, throwError } from "rxjs";

import { ConfigService } from "../../../core/services/config.service";
import { PresignedUpload } from "../models/presigned-upload.model";
import { MessageSignals } from "../../../core/signals/message.signals";
import { FileMetadata } from "../models/metadata.model";
import { PresignedDownload } from "../models/presigned-download.model";

/**
 * FileService
 * -----------
 * - getPresignedUrl
 * - uploadToS3
 */
@Injectable({ providedIn: "root" })
export class FileService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);
  private readonly messageSignals = new MessageSignals();

  /** Published signals */
  message = this.messageSignals.message;
  errorStatus = this.messageSignals.errorStatus;
  errorPath = this.messageSignals.errorPath;
  errorTimestamp = this.messageSignals.errorTimestamp;

  /**
   * Get a presigned URL
   *
   * @param file file object
   * @param expirationDays optional expiration days
   * @returns
   */
  getPresignedUploadUrl(
    file: File,
    expirationDays?: number,
  ): Observable<PresignedUpload> {
    return this.http
      .post<PresignedUpload>(
        this.config.getEndpointUrl("upload"),
        {
          filename: file.name,
          contentType: file.type,
          size: file.size,
          expirationDays,
        },
        { withCredentials: true },
      )
      .pipe(
        tap(() => {
          this.messageSignals.clear();
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Failed to get presigned URL");

          return throwError(() => error);
        }),
      );
  }

  /**
   * Upload to S3
   *
   * @param uploadUrl file upload URL
   * @param file file object
   * @returns
   */
  uploadToS3(uploadUrl: string, file: File) {
    return this.http.put(uploadUrl, file, {
      headers: { "Content-Type": file.type },
      reportProgress: true,
      observe: "events",
    });
  }

  /**
   * Get my files
   *
   * @returns array of file metadata
   */
  getMyFiles(): Observable<FileMetadata[]> {
    return this.http
      .get<
        FileMetadata[]
      >(`${this.config.getEndpointUrl("files")}/my`, { withCredentials: true })
      .pipe(
        tap(() => {
          this.message.set(null);
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Failed to get user files");

          return throwError(() => error);
        }),
      );
  }

  /**
   * Get file metadata and its download link
   *
   * @param id file id
   * @returns file details
   */
  getPresignedDownloadUrl(token: string): Observable<PresignedDownload> {
    return this.http
      .get<PresignedDownload>(
        `${this.config.getEndpointUrl("download")}/${token}`,
        {
          withCredentials: true,
        },
      )
      .pipe(
        tap(() => {
          this.message.set(null);
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Failed to get file details");

          return throwError(() => error);
        }),
      );
  }

  /**
   * Delete a user file
   *
   * @param tokenString the file token string
   */
  deleteMyFile(tokenString: string): Observable<void> {
    return this.http
      .delete<void>(
        `${this.config.getEndpointUrl("files")}/my/${tokenString}`,
        { withCredentials: true },
      )
      .pipe(
        tap(() => {
          this.message.set(null);
        }),
        catchError((error) => {
          this.messageSignals.error(error, "Failed to delete file");

          return throwError(() => error);
        }),
      );
  }

  clearMessage(): void {
    this.messageSignals.clear();
  }
}
