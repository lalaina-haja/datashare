/**
 * Defines an api error response
 */
export interface ApiError {
  /** The HTTP status code of the error response */
  status: number | null;

  /** The error message describing what went wrong */
  message: string;

  /** The request path that caused the error */
  path: string | null;

  /** The date and time when the error occurred */
  timestamp: string | null;
}
