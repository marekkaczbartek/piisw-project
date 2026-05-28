import { APIRequestContext, request } from '@playwright/test';

const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080';

export interface AuthResponse {
  token: string;
  id: string;
  email: string;
  role: 'PASSENGER' | 'INSPECTOR';
  firstName: string;
  lastName: string;
}

export interface PurchaseResponse {
  id: string;
  ticketType: 'SINGLE_USE' | 'TIME_BASED' | 'PERIOD';
  discountType: 'NORMAL' | 'REDUCED';
  price: number;
  durationMinutes: number | null;
  boughtAt: string;
  expiresAt: string | null;
}

export function nowLocal(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  );
}

export async function newApiContext(): Promise<APIRequestContext> {
  return request.newContext({ baseURL: BACKEND_URL });
}

export async function register(
  ctx: APIRequestContext,
  body: { email: string; password: string; firstName: string; lastName: string },
): Promise<AuthResponse> {
  const res = await ctx.post('/register', { data: body });
  if (!res.ok()) throw new Error(`register failed: ${res.status()} ${await res.text()}`);
  return res.json();
}

export async function login(
  ctx: APIRequestContext,
  body: { email: string; password: string },
): Promise<AuthResponse> {
  const res = await ctx.post('/login', { data: body });
  if (!res.ok()) throw new Error(`login failed: ${res.status()} ${await res.text()}`);
  return res.json();
}

export async function purchase(
  ctx: APIRequestContext,
  token: string,
  body: {
    ticketType: 'SINGLE_USE' | 'TIME_BASED' | 'PERIOD';
    discountType: 'NORMAL' | 'REDUCED';
    durationMinutes: number | null;
    // Override boughtAt (defaults to local now). Pass a past timestamp to make
    // PERIOD tickets validate as valid regardless of client/server timezone skew.
    boughtAt?: string;
  },
): Promise<PurchaseResponse> {
  const { boughtAt, ...rest } = body;
  const res = await ctx.post('/purchases', {
    headers: { Authorization: `Bearer ${token}` },
    data: { ...rest, boughtAt: boughtAt ?? nowLocal() },
  });
  if (!res.ok()) throw new Error(`purchase failed: ${res.status()} ${await res.text()}`);
  return res.json();
}

// ISO LocalDateTime (no zone) for `hours` ago, for deterministic validity windows.
export function hoursAgoLocal(hours: number): string {
  const d = new Date(Date.now() - hours * 3_600_000);
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  );
}

export function uniqueEmail(prefix = 'pw'): string {
  const rand = Math.random().toString(36).slice(2, 10);
  return `${prefix}-${Date.now()}-${rand}@test.local`;
}
