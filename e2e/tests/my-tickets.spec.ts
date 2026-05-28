import { test, expect } from '@playwright/test';
import { hoursAgoLocal, newApiContext, purchase, register, uniqueEmail } from '../fixtures/api';
import { injectAuth } from '../fixtures/auth';

test.describe('my tickets', () => {
  test('fresh passenger sees empty-pocket state', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await register(ctx, {
      email: uniqueEmail(),
      password: 'passw0rd',
      firstName: 'No',
      lastName: 'Tickets',
    });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/my-tickets');
    await expect(page.locator('.empty')).toBeVisible();
    await expect(page.locator('.empty')).toContainText(/kieszeń/);
    await expect(page.locator('.ticket-card')).toHaveCount(0);
  });

  test('two purchases appear with QR codes and ticket IDs', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await register(ctx, {
      email: uniqueEmail(),
      password: 'passw0rd',
      firstName: 'Two',
      lastName: 'Tix',
    });
    await purchase(ctx, auth.token, {
      ticketType: 'SINGLE_USE',
      discountType: 'NORMAL',
      durationMinutes: null,
    });
    await purchase(ctx, auth.token, {
      ticketType: 'TIME_BASED',
      discountType: 'NORMAL',
      durationMinutes: 20,
    });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/my-tickets');
    await expect(page.locator('.ticket-card')).toHaveCount(2);
    // QR codes render as <canvas> or <svg> inside app-qr-code; just assert the component is present.
    await expect(page.locator('app-qr-code')).toHaveCount(2);
  });

  test('pagination shows page 2 when there are more than 5 purchases', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await register(ctx, {
      email: uniqueEmail(),
      password: 'passw0rd',
      firstName: 'Many',
      lastName: 'Tix',
    });
    for (let i = 0; i < 7; i++) {
      await purchase(ctx, auth.token, {
        ticketType: 'SINGLE_USE',
        discountType: 'NORMAL',
        durationMinutes: null,
      });
    }
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/my-tickets');
    await expect(page.locator('.pager')).toBeVisible();
    await expect(page.locator('.pager__indicator')).toContainText(/Strona 1 z 2/);

    await page.getByRole('button', { name: /Następna/ }).click();
    await expect(page.locator('.pager__indicator')).toContainText(/Strona 2 z 2/);
    // Last page: 7 total, 5 per page → 2 on page 2.
    await expect(page.locator('.ticket-card')).toHaveCount(2);
  });

  test('tabs split usable tickets from history', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await register(ctx, {
      email: uniqueEmail(),
      password: 'passw0rd',
      firstName: 'Tab',
      lastName: 'Split',
    });
    // Fresh single-use → still usable → "Aktualne" tab.
    await purchase(ctx, auth.token, {
      ticketType: 'SINGLE_USE',
      discountType: 'NORMAL',
      durationMinutes: null,
    });
    // 30-day PERIOD bought 31 days ago → already expired → "Historia" tab.
    await purchase(ctx, auth.token, {
      ticketType: 'PERIOD',
      discountType: 'NORMAL',
      durationMinutes: 43200,
      boughtAt: hoursAgoLocal(24 * 31),
    });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/my-tickets');

    // Default tab is "Aktualne" and shows only the usable ticket.
    await expect(page.getByRole('button', { name: /Aktualne/ })).toHaveClass(/active/);
    await expect(page.locator('.ticket-card')).toHaveCount(1);

    // Switching to history reveals the expired ticket instead.
    await page.getByRole('button', { name: /Historia/ }).click();
    await expect(page.locator('.ticket-card')).toHaveCount(1);
    await expect(page.locator('.tc-status.expired')).toBeVisible();
  });
});
