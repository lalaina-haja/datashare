/** Register and Login request dto */
export interface AuthRequest {
  /** user's email */
  email: string;

  /** user's password */
  password: string;
}
