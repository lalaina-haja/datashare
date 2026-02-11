// src/app/core/signals/message.signals.ts

import { signal } from "@angular/core";

export class MessageSignals {
  /** Success or Error message */
  message = signal<string | null>(null);

  /** Error status code */
  errorStatus = signal<number | null>(null);

  /** Original request path */
  errorPath = signal<string | null>(null);

  /** Error timestamp */
  errorTimestamp = signal<string | null>(null);

  /**
   * Clear all signals
   */
  clear(): void {
    this.message.set(null);
    this.errorStatus.set(null);
    this.errorPath.set(null);
    this.errorTimestamp.set(null);
  }

  /**
   * Set message and clear all other signals
   *
   * @param message success message
   */
  success(message: string) {
    this.clear();
    this.message.set(message);
  }

  /**
   * Set signals from error object
   *
   * @param error error object
   * @param defaultMessage
   */
  error(error: any, defaultMessage: string) {
    this.message.set(
      error.error?.errors?.email ||
        error.error?.errors?.password ||
        error.error?.message ||
        defaultMessage,
    );

    this.errorStatus.set(error.error?.status || null);
    this.errorPath.set(error.error?.path || null);
    this.errorTimestamp.set(error.error?.timestamp || null);
  }
}
