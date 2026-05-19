import { PurchaseHistoryItem } from './mine.types';
import { formatDuration, formatTimeUntil, purchaseStatus } from './purchase.status';

function makeItem(over: Partial<PurchaseHistoryItem> = {}): PurchaseHistoryItem {
  return {
    id: 'p1',
    ticketType: 'SINGLE_USE',
    discountType: 'NORMAL',
    price: 4.6,
    durationMinutes: null,
    boughtAt: '2026-05-19T10:00:00',
    punchedAt: null,
    punchedIn: null,
    expiresAt: null,
    ...over,
  };
}

const NOW = new Date('2026-05-19T12:00:00Z');

describe('purchaseStatus', () => {
  describe('SINGLE_USE', () => {
    it('is unused when not punched', () => {
      const s = purchaseStatus(makeItem({ ticketType: 'SINGLE_USE', punchedAt: null }), NOW);
      expect(s).toEqual({ kind: 'unused', label: 'Nieskasowany' });
    });

    it('is active once punched (never expires by clock)', () => {
      const s = purchaseStatus(
        makeItem({
          ticketType: 'SINGLE_USE',
          punchedAt: '2026-05-19T11:00:00',
          expiresAt: '2026-05-19T10:00:00', // ignored for SINGLE_USE
        }),
        NOW,
      );
      expect(s).toEqual({ kind: 'active', label: 'Skasowany' });
    });
  });

  describe('PERIOD', () => {
    it('is active when expiresAt is in the future', () => {
      const s = purchaseStatus(
        makeItem({ ticketType: 'PERIOD', expiresAt: '2026-06-19T12:00:00Z' }),
        NOW,
      );
      expect(s).toEqual({ kind: 'active', label: 'Aktywny' });
    });

    it('is expired when expiresAt has passed', () => {
      const s = purchaseStatus(
        makeItem({ ticketType: 'PERIOD', expiresAt: '2026-04-19T12:00:00Z' }),
        NOW,
      );
      expect(s).toEqual({ kind: 'expired', label: 'Wygasły' });
    });

    it('is active when expiresAt is missing (treated as not-yet-expired)', () => {
      const s = purchaseStatus(makeItem({ ticketType: 'PERIOD', expiresAt: null }), NOW);
      expect(s.kind).toBe('active');
    });
  });

  describe('TIME_BASED', () => {
    it('is unused when not punched', () => {
      const s = purchaseStatus(
        makeItem({ ticketType: 'TIME_BASED', durationMinutes: 60, punchedAt: null }),
        NOW,
      );
      expect(s).toEqual({ kind: 'unused', label: 'Nieaktywowany' });
    });

    it('is active while inside the validity window', () => {
      const s = purchaseStatus(
        makeItem({
          ticketType: 'TIME_BASED',
          durationMinutes: 60,
          punchedAt: '2026-05-19T11:30:00Z',
          expiresAt: '2026-05-19T12:30:00Z',
        }),
        NOW,
      );
      expect(s).toEqual({ kind: 'active', label: 'Aktywny' });
    });

    it('is expired once expiresAt has passed', () => {
      const s = purchaseStatus(
        makeItem({
          ticketType: 'TIME_BASED',
          durationMinutes: 60,
          punchedAt: '2026-05-19T10:00:00Z',
          expiresAt: '2026-05-19T11:00:00Z',
        }),
        NOW,
      );
      expect(s).toEqual({ kind: 'expired', label: 'Wygasły' });
    });
  });

  it('defaults `now` to the current time when omitted', () => {
    const s = purchaseStatus(makeItem({ ticketType: 'SINGLE_USE' }));
    expect(s.kind).toBe('unused');
  });
});

describe('formatDuration', () => {
  it('returns the single-ride label for null minutes', () => {
    expect(formatDuration(null)).toBe('jeden przejazd');
  });

  it('formats sub-hour as minutes', () => {
    expect(formatDuration(20)).toBe('20 minut');
    expect(formatDuration(59)).toBe('59 minut');
  });

  it('formats sub-day as hours', () => {
    expect(formatDuration(60)).toBe('1 godzin');
    expect(formatDuration(180)).toBe('3 godzin');
  });

  it('formats one day as "1 dzień"', () => {
    expect(formatDuration(1440)).toBe('1 dzień');
  });

  it('formats multi-day spans rounded to days', () => {
    expect(formatDuration(7 * 1440)).toBe('7 dni');
    expect(formatDuration(30 * 1440)).toBe('30 dni');
  });
});

describe('formatTimeUntil', () => {
  it('returns null when target is in the past', () => {
    expect(formatTimeUntil('2026-05-19T10:00:00Z', NOW)).toBeNull();
  });

  it('returns null at the exact moment of expiry', () => {
    expect(formatTimeUntil('2026-05-19T12:00:00Z', NOW)).toBeNull();
  });

  it('formats sub-day remaining as HH:MM:SS', () => {
    // 1h 02m 03s ahead of NOW
    expect(formatTimeUntil('2026-05-19T13:02:03Z', NOW)).toBe('01:02:03');
  });

  it('formats multi-day remaining with a "X dni" prefix', () => {
    // 2 days + 03:04:05 ahead of NOW
    expect(formatTimeUntil('2026-05-21T15:04:05Z', NOW)).toBe('2 dni · 03:04:05');
  });

  it('pads single-digit hours/minutes/seconds', () => {
    // 09:05:07 ahead
    expect(formatTimeUntil('2026-05-19T21:05:07Z', NOW)).toBe('09:05:07');
  });
});
