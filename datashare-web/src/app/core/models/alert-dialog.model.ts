/**
 * Defines an alert dialog data
 */
export interface AlertDialogData {
  /** the alert dialog title  */
  title: string;

  /** error message or success message */
  message: string;

  /** response status */
  status?: number;

  /** the request path causing the error */
  path?: string;

  /** the timestamp of the original request */
  timestamp?: string;

  /** list of detailed errors */
  errors?: Record<string, string>;
}
