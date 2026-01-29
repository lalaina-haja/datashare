/**
 * Defines an authentication request
 */
export interface AuthRequest {
  /** The user's email address */
  email: string;

  /** The user's password */
  password: string;
}
