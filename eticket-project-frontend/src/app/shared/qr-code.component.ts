import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-qr-code',
  template: `<div class="qr-code" [innerHTML]="svg()" [style.width.px]="size()"></div>`,
  styles: `.qr-code { line-height: 0; display: inline-block; } .qr-code :first-child { display: block; }`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QrCodeComponent {
  readonly value = input.required<string>();
  readonly size = input<number>(120);

  private readonly sanitizer = inject(DomSanitizer);
  protected readonly svg = signal<SafeHtml>('');

  constructor() {
    effect(async () => {
      const out = await QRCode.toString(this.value(), {
        type: 'svg',
        margin: 0,
        width: this.size(),
        color: { dark: '#0B0B0B', light: '#FCFBF8' },
      });
      this.svg.set(this.sanitizer.bypassSecurityTrustHtml(out));
    });
  }
}
