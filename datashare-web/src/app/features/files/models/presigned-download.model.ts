/** Presigned Download URL response dto */
export interface PresignedDownload {
  /** file name */
  filename: string;

  /** content type */
  contentType: string;

  /** file size */
  size: number;

  /** download URL */
  downloadUrl: string;

  /** creation date */
  createdAt: string;

  /** expiration date */
  expiresAt: string;
}
