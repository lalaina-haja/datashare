/** Presigned Upload URL response dto */
export interface PresignedUpload {
  /** Presigned upload URL */
  uploadUrl: string;

  /** Download token string */
  tokenString: string;

  /** Token expiration */
  expiresAt: string;
}
