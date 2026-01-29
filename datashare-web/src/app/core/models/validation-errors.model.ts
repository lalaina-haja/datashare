import { ApiError } from './api-error.model';

/**
 * Defines a validation error response
 */
export interface ValidationError extends ApiError {
  /** List of validation errors (field -> error message) */
  errors: Record<string, string>;
}
