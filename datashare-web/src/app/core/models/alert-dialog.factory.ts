import { AlertDialogData } from './alert-dialog.model';
import { ApiError } from './api-error.model';
import { ValidationError } from './validation-errors.model';

/**
 * Creates an alert dialog data for an api error
 *
 * @param error the api error
 * @returns an alert dialog data
 */
export function createDialogForApiError(error: ApiError): AlertDialogData {
  return {
    title: 'Error',
    message: error.message,
    path: error.path,
    timestamp: error.timestamp,
    validationErrors: null,
    status: error.status,
  };
}

/**
 * Creates an alert dialog data for a validation error
 *
 * @param error the validation error
 * @returns an alert dialog data
 */
export function createDialogForValidationError(error: ValidationError): AlertDialogData {
  return {
    title: 'Error',
    message: error.message,
    validationErrors: error.errors,
    path: error.path,
    timestamp: error.timestamp,
    status: error.status,
  };
}

/**
 * Creates an alert dialog data for a success message
 *
 * @param message the message
 * @returns an alert dialog data
 */
export function createDialogForSuccessMessage(message: string): AlertDialogData {
  return {
    title: 'Success',
    message: message,
    validationErrors: null,
    path: null,
    timestamp: null,
    status: null,
  };
}
