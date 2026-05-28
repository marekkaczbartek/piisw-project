import { test, expect } from '@playwright/test';
import { login, newApiContext, register, uniqueEmail } from '../fixtures/api';
import { injectAuth } from '../fixtures/auth';
import { SEED_PASSENGER } from '../fixtures/seed';

test.describe('browse + purchase', () => {
  test('logged-in passenger sees ticket categories populated from /tickets', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await login(ctx, { email: SEED_PASSENGER.email, password: SEED_PASSENGER.password });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/browse');
    await expect(page.getByRole('heading', { name: /Oferta/ })).toBeVisible();

    // 3 category columns (SINGLE_USE, TIME_BASED, PERIOD).
    await expect(page.locator('.cat-title')).toHaveCount(3);
    // At least one normal-fare option button per category.
    await expect(page.locator('.opt-btn')).not.toHaveCount(0);
  });

  test('full purchase flow: open checkout → confirm → land on /mine with new ticket', async ({ page }) => {
    // Fresh passenger so /mine starts empty.
    const ctx = await newApiContext();
    const email = uniqueEmail();
    const auth = await register(ctx, { email, password: 'passw0rd', firstName: 'Buy', lastName: 'Er' });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/browse');
    // First available NORMAL option (deterministic: first .opt-btn that isn't .reduced).
    const firstNormal = page.locator('.opt-btn').filter({ hasNot: page.locator('.reduced') }).first();
    await firstNormal.click();

    // Checkout modal opens.
    await expect(page.locator('.modal-back')).toBeVisible();
    await expect(page.locator('.kv-table')).toContainText(email);

    await page.getByRole('button', { name: /Kupuję/ }).click();

    await page.waitForURL('**/mine');
    await expect(page.locator('.ticket-card')).toHaveCount(1);
    await expect(page.locator('.ticket-card .tc-meta')).toContainText(/[0-9a-f-]{36}/);
  });

  test('closing checkout modal returns to browse without purchase', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await login(ctx, { email: SEED_PASSENGER.email, password: SEED_PASSENGER.password });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/browse');
    await page.locator('.opt-btn').first().click();
    await expect(page.locator('.modal-back')).toBeVisible();

    await page.getByRole('button', { name: 'Anuluj' }).click();
    await expect(page.locator('.modal-back')).toBeHidden();
    await expect(page).toHaveURL(/\/browse$/);
  });
});
