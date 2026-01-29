/**
 * Defines an alert dialog data
 */
export interface AlertDialogData {
  /** the alert dialog title  */
  title: string;

  /** error message or success message */
  message: string;

  /** response status */
  status: number | null;

  /** the request path causing the error */
  path: string | null;

  /** the timestamp of the original request */
  timestamp: string | null;

  /** list of validation errors */
  validationErrors: Record<string, string> | null; // erreurs de validation
}
