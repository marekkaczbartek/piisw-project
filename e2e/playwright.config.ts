import { defineConfig, devices } from '@playwright/test';

// Default to the Angular dev server (ng serve) so the frontend uses
// environment.ts (apiUrl=http://localhost:8080), which matches the dockerized backend.
// Override with E2E_FRONTEND_URL=http://localhost:8081 to test the production build,
// but note that build points at https://api.eticket.linek.dev and won't talk to localhost.
const FRONTEND_URL = process.env.E2E_FRONTEND_URL ?? 'http://localhost:4200';
const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080';
const SKIP_WEBSERVER = process.env.E2E_SKIP_WEBSERVER === '1';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  timeout: 30_000,
  expect: { timeout: 5_000 },
  use: {
    baseURL: FRONTEND_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    extraHTTPHeaders: { Accept: 'application/json' },
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        permissions: ['camera'],
      },
    },
  ],
  webServer: SKIP_WEBSERVER
    ? undefined
    : {
        command: 'npm --prefix ../eticket-project-frontend run start -- --port 4200',
        url: FRONTEND_URL,
        reuseExistingServer: true,
        timeout: 180_000,
        stdout: 'ignore',
        stderr: 'pipe',
      },
  metadata: { backendUrl: BACKEND_URL },
});
