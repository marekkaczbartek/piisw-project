# eticket e2e

Playwright end-to-end tests for the eticket frontend + backend.

## Prerequisites

- Node 22+
- Docker (for Postgres + Spring backend)
- The Angular frontend dependencies installed (`cd ../eticket-project-frontend && npm install`)

## One-time setup

```bash
npm install
npx playwright install --with-deps chromium
```

## How it runs

- **Frontend**: Playwright's `webServer` starts `ng serve` on port 4200 (uses
  `environment.ts` → `apiUrl=http://localhost:8080`). If a dev server is already
  running on 4200 it's reused.
- **Backend + Postgres**: must be started manually via docker compose. The
  backend seeds two users (`passenger@example.com` / `passenger`,
  `inspector@example.com` / `inspector`) and 8 ticket variants when
  `app.seed.enabled=true` (already the default).

## Run

```bash
# 1. Start backend + Postgres (only needed once per session)
npm run backend:up
npm run backend:wait

# 2. Run tests (will start ng serve automatically)
npm test
```

Or do all of it in one shot:

```bash
npm run e2e
```

Useful flags:

```bash
npm run test:ui       # Playwright UI mode (best for debugging)
npm run test:headed   # see the browser
npm run report        # open the HTML report after a run
```

## Stop / reset

```bash
npm run backend:down  # docker compose down -v (wipes Postgres volume → fresh seed next time)
```

## Environment overrides

| Variable             | Default                  | Notes                                              |
| -------------------- | ------------------------ | -------------------------------------------------- |
| `E2E_FRONTEND_URL`   | `http://localhost:4200`  | Where Playwright opens pages.                      |
| `E2E_BACKEND_URL`    | `http://localhost:8080`  | Used by API helpers + the wait script.             |
| `E2E_SKIP_WEBSERVER` | unset                    | Set to `1` to skip auto-starting `ng serve`.       |

## Layout

```
e2e/
  fixtures/         # api helpers (register/login/purchase), auth localStorage injector, seed constants
  scripts/          # wait-for-stack.mjs (polls /tickets until backend is up)
  tests/            # auth, guards, browse-purchase, my-tickets, inspector
  playwright.config.ts
```

## Notes on strategy

- Most tests hit the **real backend** (data, validation, contract).
- Tests register a **fresh unique-email user** instead of mutating seed
  users — keeps specs isolated even though Postgres persists across tests.
- The seeded **inspector** account is used read-only-ish (only POSTs
  `/validations`), so cross-test pollution is negligible.
- Inspector edge cases (invalid / 404 / 500) use `page.route()` to mock
  `POST /validations`, since those states are hard to reproduce on demand.
- The 8081 (nginx, production build) image points at
  `https://api.eticket.linek.dev`, so tests target the dev server on 4200.

## Known issues surfaced by these tests

- **Duplicate registration returns 403, not 409.** `AuthService.register` throws
  `409 CONFLICT`, but the error re-dispatch to `/error` re-enters the security
  chain (where `/error` is not `permitAll`) and is rewritten to `403`. The
  frontend only maps `409 → "Konto już istnieje"`, so a duplicate email shows the
  generic "Rejestracja nie powiodła się" message instead. The auth spec asserts
  the current behavior (registration blocked + error shown), not the intended copy.
