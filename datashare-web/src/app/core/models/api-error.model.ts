/**
 * Defines an api error response
 */
export interface ApiError {
  /** The error message describing what went wrong */
  message: string;

  /** The HTTP status code of the error response */
  status: number;

  /** List of validation errors (field -> error message) */
  errors?: Record<string, string>;

  /** The request path that caused the error */
  path: string;

  /** The date and time when the error occurred */
  timestamp: string;
}
