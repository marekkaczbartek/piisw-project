import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  inject,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { Html5Qrcode } from 'html5-qrcode';

const REGION_ID = 'qr-scanner-region';

@Component({
  selector: 'app-qr-scanner',
  template: `
    <div class="qr-scanner">
      <div #region [id]="regionId" class="qr-scanner__region"></div>
      <div class="qr-scanner__corner tl"></div>
      <div class="qr-scanner__corner tr"></div>
      <div class="qr-scanner__corner bl"></div>
      <div class="qr-scanner__corner br"></div>
      @if (error()) {
        <div class="qr-scanner__error">{{ error() }}</div>
      } @else if (!ready()) {
        <div class="qr-scanner__hint">Uruchamianie kamery…</div>
      }
    </div>
  `,
  styleUrl: './qr-scanner.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QrScannerComponent implements OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly region = viewChild.required<ElementRef<HTMLDivElement>>('region');

  readonly decoded = output<string>();

  protected readonly regionId = REGION_ID;
  protected readonly ready = signal(false);
  protected readonly error = signal<string | null>(null);

  private scanner: Html5Qrcode | null = null;

  constructor() {
    afterNextRender(() => this.start());
  }

  ngOnDestroy(): void {
    this.stop();
  }

  private async start(): Promise<void> {
    // html5-qrcode looks the element up by id, so make it unique per instance.
    const id = `${REGION_ID}-${Math.random().toString(36).slice(2)}`;
    this.region().nativeElement.id = id;

    try {
      this.scanner = new Html5Qrcode(id, { verbose: false });
      await this.scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 220, height: 220 } },
        (decodedText) => this.decoded.emit(decodedText),
        () => {
          /* per-frame failures are noise; ignore */
        },
      );
      this.ready.set(true);
    } catch (err) {
      this.error.set(this.formatError(err));
    }
  }

  private async stop(): Promise<void> {
    if (!this.scanner) return;
    try {
      if (this.scanner.isScanning) await this.scanner.stop();
      await this.scanner.clear();
    } catch {
      /* nothing useful we can do at teardown */
    }
    this.scanner = null;
  }

  private formatError(err: unknown): string {
    const message = err instanceof Error ? err.message : String(err);
    if (/permission|notallowed/i.test(message)) {
      return 'Brak dostępu do kamery — zezwól w ustawieniach przeglądarki.';
    }
    if (/notfound|nocamera/i.test(message)) {
      return 'Nie wykryto kamery w tym urządzeniu.';
    }
    return `Nie udało się uruchomić kamery: ${message}`;
  }
}
