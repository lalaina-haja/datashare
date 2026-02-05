/** Register and Login response dto */
export interface AuthResponse {
  /** Success message */
  message: string;

  /** The user's email */
  email: string;

  /** The user's authorities */
  authorities: string[];
}
