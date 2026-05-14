import { groupTickets, variantLabel } from './tickets.grouping';
import { TicketResponse } from './tickets.types';

const t = (
  ticketType: TicketResponse['ticketType'],
  discountType: TicketResponse['discountType'],
  price: number,
  durationMinutes: number | null,
): TicketResponse => ({ ticketType, discountType, price, durationMinutes });

describe('groupTickets', () => {
  it('returns three columns in fixed order even when input is empty', () => {
    const cols = groupTickets([]);

    expect(cols.map((c) => c.type)).toEqual(['SINGLE_USE', 'TIME_BASED', 'PERIOD']);
    expect(cols.every((c) => c.variants.length === 0)).toBeTrue();
  });

  it('groups by duration and merges NORMAL + REDUCED into one variant', () => {
    const cols = groupTickets([
      t('SINGLE_USE', 'NORMAL', 3.4, null),
      t('SINGLE_USE', 'REDUCED', 1.7, null),
    ]);
    const single = cols.find((c) => c.type === 'SINGLE_USE')!;

    expect(single.variants).toHaveSize(1);
    expect(single.variants[0].prices).toEqual({ NORMAL: 3.4, REDUCED: 1.7 });
  });

  it('sorts variants by durationMinutes ascending (null first)', () => {
    const cols = groupTickets([
      t('TIME_BASED', 'NORMAL', 6.0, 60),
      t('TIME_BASED', 'NORMAL', 4.4, 20),
      t('TIME_BASED', 'NORMAL', 8.8, 120),
    ]);
    const time = cols.find((c) => c.type === 'TIME_BASED')!;

    expect(time.variants.map((v) => v.durationMinutes)).toEqual([20, 60, 120]);
  });

  it('keeps a discount slot empty when only one tier is offered', () => {
    const cols = groupTickets([t('PERIOD', 'NORMAL', 100, 60 * 24 * 30)]);
    const period = cols.find((c) => c.type === 'PERIOD')!;

    expect(period.variants[0].prices['NORMAL']).toBe(100);
    expect(period.variants[0].prices['REDUCED']).toBeUndefined();
  });
});

describe('variantLabel', () => {
  it('labels SINGLE_USE as Przejazd regardless of duration', () => {
    expect(variantLabel('SINGLE_USE', { durationMinutes: null, prices: {} })).toBe('Przejazd');
  });

  it('formats TIME_BASED under 60 min in minutes', () => {
    expect(variantLabel('TIME_BASED', { durationMinutes: 20, prices: {} })).toBe('20 min');
  });

  it('formats TIME_BASED >= 60 min in hours', () => {
    expect(variantLabel('TIME_BASED', { durationMinutes: 60, prices: {} })).toBe('1 godz.');
    expect(variantLabel('TIME_BASED', { durationMinutes: 120, prices: {} })).toBe('2 godz.');
  });

  it('formats PERIOD as 1 dzień / N dni', () => {
    expect(variantLabel('PERIOD', { durationMinutes: 60 * 24, prices: {} })).toBe('1 dzień');
    expect(variantLabel('PERIOD', { durationMinutes: 60 * 24 * 30, prices: {} })).toBe('30 dni');
  });

  it('returns an em-dash when duration is missing for non-single-use types', () => {
    expect(variantLabel('TIME_BASED', { durationMinutes: null, prices: {} })).toBe('—');
  });
});
