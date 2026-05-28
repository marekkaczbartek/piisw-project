// Poll the frontend and backend until both respond, or fail after timeout.
const BACKEND = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080';
const TIMEOUT_MS = 120_000;
const INTERVAL_MS = 1_500;

const targets = [{ name: 'backend /tickets', url: `${BACKEND}/tickets` }];

const start = Date.now();
const ready = new Set();

while (Date.now() - start < TIMEOUT_MS) {
  await Promise.all(
    targets.map(async (t) => {
      if (ready.has(t.name)) return;
      try {
        const res = await fetch(t.url, { method: 'GET' });
        if (res.status < 500) {
          ready.add(t.name);
          console.log(`[wait] ${t.name} ready (${res.status})`);
        }
      } catch {
        /* not up yet */
      }
    }),
  );
  if (ready.size === targets.length) {
    console.log('[wait] stack ready');
    process.exit(0);
  }
  await new Promise((r) => setTimeout(r, INTERVAL_MS));
}

console.error('[wait] timed out waiting for', targets.filter((t) => !ready.has(t.name)));
process.exit(1);
