// src/app/auth/signals/auth.signals.ts

import { signal, computed } from "@angular/core";
import { User } from "../../../core/models/user.model";

export class AuthSignals {
  /** Utilisateur connecté ou null */
  user = signal<User | null>(null);

  /** True si un utilisateur est connecté */
  isAuthenticated = computed(() => this.user() !== null);

  /** Message d’information ou d’erreur */
  message = signal<string | null>(null);

  /** Statut de l’erreur */
  errorStatus = signal<number | null>(null);

  /** Chemin de l’erreur */
  errorPath = signal<string | null>(null);

  /** Timestamp de l’erreur */
  errorTimestamp = signal<string | null>(null);
}
