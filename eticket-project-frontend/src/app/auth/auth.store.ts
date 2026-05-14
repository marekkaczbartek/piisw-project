import { Injectable, computed, signal } from '@angular/core';
import { AuthUser } from './auth.models';

const STORAGE_KEY = 'auth';

function readStored(): AuthUser | null {
  if (typeof localStorage === 'undefined') return null;
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _user = signal<AuthUser | null>(readStored());

  readonly user = this._user.asReadonly();
  readonly token = computed(() => this._user()?.token ?? null);
  readonly isLoggedIn = computed(() => this._user() !== null);

  setSession(user: AuthUser): void {
    this._user.set(user);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  clear(): void {
    this._user.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }
}
