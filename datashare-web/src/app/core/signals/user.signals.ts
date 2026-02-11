// src/app/core/signals/user.signals.ts

import { signal, computed } from "@angular/core";
import { User } from "../models/user.model";

export class UserSignals {
  /** The connected user */
  user = signal<User | null>(null);

  /** Is an user connected ? */
  isAuthenticated = computed(() => this.user() !== null);
}
