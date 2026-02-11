/**
 * File metadata
 */
export interface FileMetadata {
  /** file name */
  filename: string;

  /** content type */
  contentType: string;

  /** file size */
  size: number;

  /** download token */
  downloadToken: string;

  /** creation date */
  createdAt: string;

  /** expiration date */
  expiresAt: string;
}
