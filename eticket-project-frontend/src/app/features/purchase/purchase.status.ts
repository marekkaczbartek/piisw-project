import { PurchaseHistoryItem, PurchaseStatus } from './purchase.types';

/**
 * Mirrors the backend's `isValidAt()` (PurchaseService.java) for UI categorisation.
 * — SINGLE_USE: 'unused' until punched, then 'active'.
 * — TIME_BASED: 'unused' until punched, 'active' while within validity, otherwise 'expired'.
 * — PERIOD:     'active' until `expiresAt` passes, then 'expired'.
 */
export function purchaseStatus(p: PurchaseHistoryItem, now: Date = new Date()): PurchaseStatus {
  if (p.ticketType === 'SINGLE_USE') {
    return p.punchedAt
      ? { kind: 'active', label: 'Skasowany' }
      : { kind: 'unused', label: 'Nieskasowany' };
  }

  if (p.ticketType === 'PERIOD') {
    if (p.expiresAt && new Date(p.expiresAt) < now) {
      return { kind: 'expired', label: 'Wygasły' };
    }
    return { kind: 'active', label: 'Aktywny' };
  }

  // TIME_BASED
  if (!p.punchedAt) return { kind: 'unused', label: 'Nieaktywowany' };
  if (p.expiresAt && new Date(p.expiresAt) < now) {
    return { kind: 'expired', label: 'Wygasły' };
  }
  return { kind: 'active', label: 'Aktywny' };
}

export function formatDuration(minutes: number | null): string {
  if (minutes == null) return 'jeden przejazd';
  if (minutes < 60) return `${minutes} minut`;
  if (minutes < 1440) return `${minutes / 60} godzin`;
  const days = Math.round(minutes / 1440);
  return days === 1 ? '1 dzień' : `${days} dni`;
}

export function formatTimeUntil(target: string, now: Date = new Date()): string | null {
  const ms = new Date(target).getTime() - now.getTime();
  if (ms <= 0) return null;
  const tot = Math.floor(ms / 1000);
  const d = Math.floor(tot / 86400);
  const h = Math.floor((tot % 86400) / 3600);
  const m = Math.floor((tot % 3600) / 60);
  const s = tot % 60;
  const pad = (n: number) => String(n).padStart(2, '0');
  return d > 0
    ? `${d} dni · ${pad(h)}:${pad(m)}:${pad(s)}`
    : `${pad(h)}:${pad(m)}:${pad(s)}`;
}
