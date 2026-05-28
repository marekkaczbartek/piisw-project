import { test, expect, Page } from '@playwright/test';
import { hoursAgoLocal, login, newApiContext, purchase, register, uniqueEmail } from '../fixtures/api';
import { injectAuth } from '../fixtures/auth';
import { SEED_INSPECTOR } from '../fixtures/seed';

async function loginInspector(page: Page): Promise<void> {
  const ctx = await newApiContext();
  const auth = await login(ctx, {
    email: SEED_INSPECTOR.email,
    password: SEED_INSPECTOR.password,
  });
  await ctx.dispose();
  await injectAuth(page, auth);
}

test.describe('inspector validation', () => {
  test('valid ticket: real purchase → inspector validates → modal shows OK', async ({ page }) => {
    // Arrange: passenger registers and buys a ticket via API.
    const ctx = await newApiContext();
    const passenger = await register(ctx, {
      email: uniqueEmail('insp-pax'),
      password: 'passw0rd',
      firstName: 'Tic',
      lastName: 'Ket',
    });
    // PERIOD tickets are valid from purchase time (no punch needed); TIME_BASED /
    // SINGLE_USE require a prior punch. boughtAt set in the past so the server-side
    // checkedAt=now() is comfortably inside the validity window despite TZ skew.
    const p = await purchase(ctx, passenger.token, {
      ticketType: 'PERIOD',
      discountType: 'NORMAL',
      durationMinutes: 43200,
      boughtAt: hoursAgoLocal(2),
    });
    await ctx.dispose();

    await loginInspector(page);
    await page.goto('/inspector');

    await page.locator('#vehicle').fill('T-04');
    await page.locator('#code').fill(p.id);
    await page.getByRole('button', { name: /Sprawdź ważność/ }).click();

    await expect(page.locator('.result-banner.ok')).toBeVisible();
    await expect(page.locator('.result-banner__title')).toContainText(/ważny/);
  });

  test('mocked invalid response: modal shows "nieważny"', async ({ page }) => {
    await loginInspector(page);

    await page.route('**/validations', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ valid: false }),
      }),
    );

    await page.goto('/inspector');
    await page.locator('#vehicle').fill('T-04');
    await page.locator('#code').fill('550e8400-e29b-41d4-a716-446655440000');
    await page.getByRole('button', { name: /Sprawdź ważność/ }).click();

    await expect(page.locator('.result-banner.bad')).toBeVisible();
    await expect(page.locator('.result-banner__title')).toContainText(/nieważny/);
  });

  test('mocked 404: modal shows error state', async ({ page }) => {
    await loginInspector(page);

    await page.route('**/validations', (route) =>
      route.fulfill({ status: 404, contentType: 'application/json', body: '{}' }),
    );

    await page.goto('/inspector');
    await page.locator('#vehicle').fill('T-04');
    await page.locator('#code').fill('550e8400-e29b-41d4-a716-446655440000');
    await page.getByRole('button', { name: /Sprawdź ważność/ }).click();

    await expect(page.locator('.result-banner.bad')).toBeVisible();
    await expect(page.locator('.form-error')).toContainText(/Nie udało się/);
  });

  test('mocked 500: modal shows generic error', async ({ page }) => {
    await loginInspector(page);

    await page.route('**/validations', (route) =>
      route.fulfill({ status: 500, contentType: 'application/json', body: '{}' }),
    );

    await page.goto('/inspector');
    await page.locator('#vehicle').fill('T-04');
    await page.locator('#code').fill('550e8400-e29b-41d4-a716-446655440000');
    await page.getByRole('button', { name: /Sprawdź ważność/ }).click();

    await expect(page.locator('.result-banner.bad')).toBeVisible();
    await expect(page.locator('.form-error')).toContainText(/Nie udało się/);
  });

  test('invalid UUID input is rejected client-side', async ({ page }) => {
    await loginInspector(page);
    await page.goto('/inspector');

    await page.locator('#vehicle').fill('T-04');
    await page.locator('#code').fill('not-a-uuid');
    await page.getByRole('button', { name: /Sprawdź ważność/ }).click();

    await expect(page.locator('.form-error')).toContainText(/UUID/);
    await expect(page.locator('.result-banner')).toHaveCount(0);
  });
});
