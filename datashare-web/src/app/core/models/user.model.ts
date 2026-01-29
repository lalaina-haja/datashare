/**
 * Defines a User
 */
export interface User {
  /** The user's email */
  email: string;

  /** The user's authorities */
  authorities?: string[];
}
