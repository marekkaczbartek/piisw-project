import { DiscountType, TicketResponse, TicketType } from './tickets.types';

export interface Variant {
  durationMinutes: number | null;
  prices: Partial<Record<DiscountType, number>>;
}

export interface Column {
  type: TicketType;
  icon: string;
  title: string;
  variants: Variant[];
}

export const TYPE_ORDER: TicketType[] = ['SINGLE_USE', 'TIME_BASED', 'PERIOD'];

export const TYPE_META: Record<TicketType, { icon: string; title: string }> = {
  SINGLE_USE: { icon: '🎫', title: 'Jednorazowy' },
  TIME_BASED: { icon: '⏱️', title: 'Czasowy' },
  PERIOD:     { icon: '📅', title: 'Okresowy' },
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
  if (type === 'TIME_BASED') return `${v.durationMinutes} min`;

  const days = Math.round(v.durationMinutes / (60 * 24));
  return days === 1 ? '1 dzień' : `${days} dni`;
}
