import { test, expect } from '@playwright/test';
import { login, newApiContext } from '../fixtures/api';
import { injectAuth } from '../fixtures/auth';
import { SEED_INSPECTOR, SEED_PASSENGER } from '../fixtures/seed';

test.describe('route guards', () => {
  test('unauthenticated /browse redirects to /login with returnUrl', async ({ page }) => {
    await page.goto('/browse');
    await page.waitForURL(/\/login/);
    expect(new URL(page.url()).searchParams.get('returnUrl')).toBe('/browse');
  });

  test('unauthenticated /mine redirects to /login', async ({ page }) => {
    await page.goto('/mine');
    await page.waitForURL(/\/login/);
    expect(new URL(page.url()).searchParams.get('returnUrl')).toBe('/mine');
  });

  test('unauthenticated /inspector redirects to /login', async ({ page }) => {
    await page.goto('/inspector');
    await page.waitForURL(/\/login/);
    expect(new URL(page.url()).searchParams.get('returnUrl')).toBe('/inspector');
  });

  test('passenger visiting /inspector is bounced to /browse', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await login(ctx, { email: SEED_PASSENGER.email, password: SEED_PASSENGER.password });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/inspector');
    await page.waitForURL('**/browse');
  });

  test('inspector visiting /browse is bounced to /inspector', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await login(ctx, { email: SEED_INSPECTOR.email, password: SEED_INSPECTOR.password });
    await ctx.dispose();
    await injectAuth(page, auth);

    await page.goto('/browse');
    await page.waitForURL('**/inspector');
  });

  test('401 from API clears auth and bounces to /login', async ({ page }) => {
    const ctx = await newApiContext();
    const auth = await login(ctx, { email: SEED_PASSENGER.email, password: SEED_PASSENGER.password });
    await ctx.dispose();
    await injectAuth(page, auth);

    // Make every backend call return 401 so the interceptor clears the session.
    await page.route(/\/(tickets|purchases|validations)(\?.*)?$/, (route) =>
      route.fulfill({ status: 401, contentType: 'application/json', body: '{}' }),
    );

    await page.goto('/browse');
    await page.waitForURL(/\/login/);
    const stored = await page.evaluate(() => window.localStorage.getItem('auth'));
    expect(stored).toBeNull();
  });
});
