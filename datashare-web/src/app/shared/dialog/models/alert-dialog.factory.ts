import { AlertDialogData } from "./alert-dialog.model";
import { ApiError } from "../../../core/models/api-error.model";

/**
 * Creates an alert dialog data for an api error
 *
 * @param error the api error
 * @returns an alert dialog data
 */
export function createDialogForApiError(error: ApiError): AlertDialogData {
  return {
    title: "Error",
    message: error.message,
    path: error.path,
    timestamp: error.timestamp,
    errors: error.errors,
    status: error.status,
  };
}

/**
 * Creates an alert dialog data for a success message
 *
 * @param message the message
 * @returns an alert dialog data
 */
export function createDialogForSuccessMessage(
  message: string,
): AlertDialogData {
  return {
    title: "Success",
    message: message,
  };
}
