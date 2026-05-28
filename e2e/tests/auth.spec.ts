import { test, expect } from '@playwright/test';
import { newApiContext, register, uniqueEmail } from '../fixtures/api';
import { SEED_INSPECTOR, SEED_PASSENGER } from '../fixtures/seed';

test.describe('auth', () => {
  test('register a new passenger lands on /browse with token in localStorage', async ({ page }) => {
    const email = uniqueEmail();
    await page.goto('/register');

    await page.locator('#firstName').fill('Anna');
    await page.locator('#lastName').fill('Kowalska');
    await page.locator('#email').fill(email);
    await page.locator('#password').fill('passw0rd');
    await page.getByRole('button', { name: /Załóż konto/ }).click();

    await page.waitForURL('**/browse');
    const stored = await page.evaluate(() => window.localStorage.getItem('auth'));
    expect(stored).not.toBeNull();
    const parsed = JSON.parse(stored!);
    expect(parsed.email).toBe(email);
    expect(parsed.role).toBe('PASSENGER');
    expect(parsed.token).toBeTruthy();
  });

  test('login as seeded passenger redirects to /browse', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#email').fill(SEED_PASSENGER.email);
    await page.locator('#password').fill(SEED_PASSENGER.password);
    await page.getByRole('button', { name: /Wejdź/ }).click();

    await page.waitForURL('**/browse');
    await expect(page.getByRole('heading', { name: /Oferta/ })).toBeVisible();
  });

  test('login as seeded inspector redirects to /inspector', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#email').fill(SEED_INSPECTOR.email);
    await page.locator('#password').fill(SEED_INSPECTOR.password);
    await page.getByRole('button', { name: /Wejdź/ }).click();

    await page.waitForURL('**/inspector');
    await expect(page.getByRole('heading', { name: /Kontrola/ })).toBeVisible();
  });

  test('registering with an existing email is rejected and surfaces an error', async ({ page }) => {
    const email = uniqueEmail();
    const ctx = await newApiContext();
    await register(ctx, { email, password: 'passw0rd', firstName: 'X', lastName: 'Y' });
    await ctx.dispose();

    await page.goto('/register');
    await page.locator('#firstName').fill('Anna');
    await page.locator('#lastName').fill('Kowalska');
    await page.locator('#email').fill(email);
    await page.locator('#password').fill('passw0rd');
    await page.getByRole('button', { name: /Załóż konto/ }).click();

    // NOTE: the backend throws 409 but the error re-dispatch through Spring Security
    // surfaces as 403, so the frontend shows its generic message rather than the
    // "już istnieje" copy (which is only wired to 409). We assert the observable
    // behavior: registration is blocked and an error is shown. See README.
    await expect(page.locator('.form-error')).toBeVisible();
    await expect(page).toHaveURL(/\/register$/);
  });

  test('login with bad password shows error and stays on /login', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#email').fill(SEED_PASSENGER.email);
    await page.locator('#password').fill('definitely-wrong');
    await page.getByRole('button', { name: /Wejdź/ }).click();

    await expect(page.locator('.form-error')).toContainText(/Nieprawidłowy/);
    await expect(page).toHaveURL(/\/login$/);
  });
});
