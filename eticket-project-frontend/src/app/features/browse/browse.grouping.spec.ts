import {
  categoryLabel,
  groupTickets,
  variantDescription,
  variantLabel,
  Variant,
} from './browse.grouping';
import { TicketResponse } from './browse.types';

describe('groupTickets', () => {
  it('returns one column per type, in fixed order, even when no tickets match', () => {
    const cols = groupTickets([]);
    expect(cols.map((c) => c.type)).toEqual(['SINGLE_USE', 'TIME_BASED', 'PERIOD']);
    expect(cols.every((c) => c.variants.length === 0)).toBeTrue();
  });

  it('merges NORMAL+REDUCED prices for the same (type, duration) into one variant', () => {
    const tickets: TicketResponse[] = [
      { ticketType: 'SINGLE_USE', durationMinutes: null, discountType: 'NORMAL',  price: 4.6 },
      { ticketType: 'SINGLE_USE', durationMinutes: null, discountType: 'REDUCED', price: 2.3 },
    ];

    const single = groupTickets(tickets).find((c) => c.type === 'SINGLE_USE')!;
    expect(single.variants).toEqual([
      { durationMinutes: null, prices: { NORMAL: 4.6, REDUCED: 2.3 } },
    ]);
  });

  it('sorts variants ascending by durationMinutes, with null first', () => {
    const tickets: TicketResponse[] = [
      { ticketType: 'TIME_BASED', durationMinutes: 1440, discountType: 'NORMAL', price: 15 },
      { ticketType: 'TIME_BASED', durationMinutes: 20,   discountType: 'NORMAL', price: 5 },
      { ticketType: 'TIME_BASED', durationMinutes: null, discountType: 'NORMAL', price: 1 },
      { ticketType: 'TIME_BASED', durationMinutes: 60,   discountType: 'NORMAL', price: 8 },
    ];

    const time = groupTickets(tickets).find((c) => c.type === 'TIME_BASED')!;
    expect(time.variants.map((v) => v.durationMinutes)).toEqual([null, 20, 60, 1440]);
  });

  it('attaches the localized title and subtitle for each column', () => {
    const cols = groupTickets([]);
    const period = cols.find((c) => c.type === 'PERIOD')!;
    expect(period.title).toBe('Okresowy');
    expect(period.subtitle).toBe('Kup raz i się nie martw.');
  });

  it('drops tickets with an unknown ticketType', () => {
    const tickets = [
      { ticketType: 'BOGUS', durationMinutes: 60, discountType: 'NORMAL', price: 1 },
      { ticketType: 'SINGLE_USE', durationMinutes: null, discountType: 'NORMAL', price: 4.6 },
    ] as unknown as TicketResponse[];

    const cols = groupTickets(tickets);
    expect(cols.flatMap((c) => c.variants).length).toBe(1);
  });
});

describe('variantLabel', () => {
  const v = (durationMinutes: number | null): Variant => ({ durationMinutes, prices: {} });

  it('always says "Przejazd" for SINGLE_USE', () => {
    expect(variantLabel('SINGLE_USE', v(null))).toBe('Przejazd');
    expect(variantLabel('SINGLE_USE', v(60))).toBe('Przejazd');
  });

  it('returns an em-dash placeholder for missing duration on TIME_BASED/PERIOD', () => {
    expect(variantLabel('TIME_BASED', v(null))).toBe('—');
    expect(variantLabel('PERIOD', v(null))).toBe('—');
  });

  it('formats TIME_BASED durations in minutes / hours / days', () => {
    expect(variantLabel('TIME_BASED', v(20))).toBe('20 minut');
    expect(variantLabel('TIME_BASED', v(60))).toBe('1 godzin');
    expect(variantLabel('TIME_BASED', v(180))).toBe('3 godzin');
    expect(variantLabel('TIME_BASED', v(1440))).toBe('1 dni');
  });

  it('formats PERIOD durations as a day count', () => {
    expect(variantLabel('PERIOD', v(1440))).toBe('1 dzień');
    expect(variantLabel('PERIOD', v(7 * 1440))).toBe('7 dni');
    expect(variantLabel('PERIOD', v(30 * 1440))).toBe('30 dni');
  });
});

describe('variantDescription', () => {
  const v = (durationMinutes: number | null): Variant => ({ durationMinutes, prices: {} });

  it('describes SINGLE_USE generically', () => {
    expect(variantDescription('SINGLE_USE', v(null))).toBe('Dowolna trasa');
  });

  it('returns an empty string when duration is null for non-SINGLE_USE', () => {
    expect(variantDescription('TIME_BASED', v(null))).toBe('');
    expect(variantDescription('PERIOD', v(null))).toBe('');
  });

  it('maps known TIME_BASED durations to their description', () => {
    expect(variantDescription('TIME_BASED', v(20))).toBe('Krótki dojazd');
    expect(variantDescription('TIME_BASED', v(60))).toBe('Spokojna podróż');
    expect(variantDescription('TIME_BASED', v(1440))).toBe('Cały dzień');
  });

  it('maps known PERIOD durations to their description', () => {
    expect(variantDescription('PERIOD', v(7 * 1440))).toBe('Tygodniowy');
    expect(variantDescription('PERIOD', v(30 * 1440))).toBe('Miesięczny');
    expect(variantDescription('PERIOD', v(90 * 1440))).toBe('Kwartalny');
  });

  it('returns an empty string for unknown durations', () => {
    expect(variantDescription('TIME_BASED', v(7))).toBe('');
    expect(variantDescription('PERIOD', v(999))).toBe('');
  });
});

describe('categoryLabel', () => {
  it('returns the localized title for each ticket type', () => {
    expect(categoryLabel('SINGLE_USE')).toBe('Jednorazowy');
    expect(categoryLabel('TIME_BASED')).toBe('Czasowy');
    expect(categoryLabel('PERIOD')).toBe('Okresowy');
  });
});
