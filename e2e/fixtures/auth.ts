import { Page } from '@playwright/test';
import { AuthResponse } from './api';

const STORAGE_KEY = 'auth';

// Inject an auth session into localStorage so the SPA picks it up on first paint.
// Use addInitScript so the value lands before any Angular code runs.
export async function injectAuth(page: Page, user: AuthResponse): Promise<void> {
  await page.addInitScript(
    ({ key, value }) => {
      window.localStorage.setItem(key, value);
    },
    { key: STORAGE_KEY, value: JSON.stringify(user) },
  );
}

export async function clearAuth(page: Page): Promise<void> {
  await page.addInitScript((key) => window.localStorage.removeItem(key), STORAGE_KEY);
}
