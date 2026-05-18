import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthUser } from '../auth/auth.models';
import {
  Variant,
  categoryLabel,
  variantDescription,
  variantLabel,
} from './browse.grouping';
import { DiscountType, TicketType } from './browse.types';
import { PurchaseApi, nowLocal } from './purchase.api';
import { PurchaseResponse } from './purchase.types';

export interface CheckoutSelection {
  type: TicketType;
  variant: Variant;
  discount: DiscountType;
  price: number;
}

@Component({
  selector: 'app-checkout-modal',
  imports: [CurrencyPipe],
  templateUrl: './checkout-modal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckoutModalComponent {
  private readonly purchases = inject(PurchaseApi);

  readonly selection = input.required<CheckoutSelection>();
  readonly user = input.required<AuthUser>();

  readonly close = output<void>();
  readonly purchased = output<PurchaseResponse>();

  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);

  protected categoryLabel(type: TicketType): string {
    return categoryLabel(type);
  }

  protected variantLabel(type: TicketType, v: Variant): string {
    return variantLabel(type, v);
  }

  protected validityCopy(): string {
    const { type, variant } = this.selection();
    if (type === 'SINGLE_USE') return 'Do momentu skasowania w pojeździe';
    const label = variantLabel(type, variant).toLowerCase();
    return type === 'TIME_BASED'
      ? `${label} od kasowania`
      : `${label} od momentu zakupu`;
  }

  protected descriptionCopy(): string {
    const { type, variant } = this.selection();
    return variantDescription(type, variant);
  }

  protected confirm(): void {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set(null);
    const { type, variant, discount } = this.selection();

    this.purchases
      .makePurchase({
        ticketType: type,
        discountType: discount,
        durationMinutes: variant.durationMinutes,
        boughtAt: nowLocal(),
      })
      .subscribe({
        next: (res) => this.purchased.emit(res),
        error: (err: HttpErrorResponse) => {
          this.busy.set(false);
          this.error.set(
            err.status === 401 || err.status === 403
              ? 'Sesja wygasła. Zaloguj się ponownie.'
              : 'Zakup nie powiódł się. Spróbuj ponownie.',
          );
        },
      });
  }

  @HostListener('document:keydown.escape')
  protected onEsc(): void {
    if (!this.busy()) this.close.emit();
  }
}
