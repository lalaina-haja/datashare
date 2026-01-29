/**
 * Defines a authentication response
 */
export interface AuthResponse {
  /** A message indicating the result of the registration process. */
  message: string;

  /** User's email. */
  email?: string;
}
