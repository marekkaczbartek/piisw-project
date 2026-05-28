import { DiscountType, TicketResponse, TicketType } from './browse.types';

export interface Variant {
  durationMinutes: number | null;
  prices: Partial<Record<DiscountType, number>>;
}

export interface Column {
  type: TicketType;
  title: string;
  subtitle: string;
  variants: Variant[];
}

export const TYPE_ORDER: TicketType[] = ['SINGLE_USE', 'TIME_BASED', 'PERIOD'];

const TYPE_META: Record<TicketType, { title: string; subtitle: string }> = {
  SINGLE_USE: { title: 'Jednorazowy', subtitle: 'Jeden przejazd. Skasuj w pojeździe.' },
  TIME_BASED: { title: 'Czasowy',     subtitle: 'Aktywuj raz i jeździj ile chcesz.' },
  PERIOD:     { title: 'Okresowy',    subtitle: 'Kup raz i się nie martw.' },
};

const TIME_DESCRIPTIONS: Record<number, string> = {
  20:   'Krótki dojazd',
  60:   'Spokojna podróż',
  1440: 'Cały dzień',
};

const PERIOD_DESCRIPTIONS: Record<number, string> = {
  [7 * 1440]:  'Tygodniowy',
  [30 * 1440]: 'Miesięczny',
  [90 * 1440]: 'Kwartalny',
};

export function groupTickets(tickets: readonly TicketResponse[]): Column[] {
  const byType = new Map<TicketType, Map<number | null, Variant>>();
  for (const type of TYPE_ORDER) byType.set(type, new Map());

  for (const ticket of tickets) {
    const bucket = byType.get(ticket.ticketType);
    if (!bucket) continue;
    const key = ticket.durationMinutes;
    const existing = bucket.get(key) ?? { durationMinutes: key, prices: {} };
    existing.prices[ticket.discountType] = ticket.price;
    bucket.set(key, existing);
  }

  return TYPE_ORDER.map((type) => {
    const variants = [...(byType.get(type)?.values() ?? [])].sort((a, b) => {
      const av = a.durationMinutes ?? -1;
      const bv = b.durationMinutes ?? -1;
      return av - bv;
    });
    return { type, ...TYPE_META[type], variants };
  });
}

export function variantLabel(type: TicketType, v: Variant): string {
  if (type === 'SINGLE_USE') return 'Przejazd';
  if (v.durationMinutes == null) return '—';
  if (type === 'TIME_BASED') {
    if (v.durationMinutes < 60) return `${v.durationMinutes} minut`;
    if (v.durationMinutes < 1440) return `${v.durationMinutes / 60} godzin`;
    return `${v.durationMinutes / 1440} dni`;
  }
  const days = Math.round(v.durationMinutes / 1440);
  return days === 1 ? '1 dzień' : `${days} dni`;
}

export function variantDescription(type: TicketType, v: Variant): string {
  if (type === 'SINGLE_USE') return 'Dowolna trasa';
  if (v.durationMinutes == null) return '';
  if (type === 'TIME_BASED') return TIME_DESCRIPTIONS[v.durationMinutes] ?? '';
  return PERIOD_DESCRIPTIONS[v.durationMinutes] ?? '';
}

export function categoryLabel(type: TicketType): string {
  return TYPE_META[type].title;
}
