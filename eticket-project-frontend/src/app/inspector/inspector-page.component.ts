import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { nowLocal } from '../browse/purchase.api';
import { QrScannerComponent } from './qr-scanner.component';
import { ResultModalComponent, ValidationResult } from './result-modal.component';
import { ValidationApi } from './validation.api';
import { ValidationEntry } from './validation.types';

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function randomId(): string {
  return Math.random().toString(36).slice(2);
}

@Component({
  selector: 'app-inspector-page',
  imports: [ReactiveFormsModule, DatePipe, QrScannerComponent, ResultModalComponent],
  templateUrl: './inspector-page.component.html',
  styleUrl: './inspector-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InspectorPageComponent {
  private readonly api = inject(ValidationApi);

  readonly form = new FormGroup({
    vehicle: new FormControl('T-04', { nonNullable: true, validators: [Validators.required] }),
    code: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  protected readonly scanning = signal(false);
  protected readonly busy = signal(false);
  protected readonly history = signal<ValidationEntry[]>([]);
  protected readonly result = signal<ValidationResult | null>(null);
  protected readonly inputError = signal<string | null>(null);

  protected toggleScanner(): void {
    this.scanning.update((on) => !on);
    this.inputError.set(null);
  }

  protected onScanned(text: string): void {
    const code = text.trim();
    if (!UUID_RE.test(code) || code === this.form.controls.code.value) return;
    this.form.controls.code.setValue(code);
    this.scanning.set(false);
    this.submit();
  }

  protected submit(): void {
    if (this.form.invalid || this.busy()) return;

    const vehicle = this.form.controls.vehicle.value.trim().toUpperCase();
    const code = this.form.controls.code.value.trim();
    if (!UUID_RE.test(code)) {
      this.inputError.set('Kod biletu powinien być UUID-em (np. 550e8400-e29b-41d4-a716-446655440000).');
      return;
    }

    this.busy.set(true);
    this.inputError.set(null);
    const checkedAt = nowLocal();

    this.api.validate({ purchaseId: code, checkedAt, checkedIn: vehicle }).subscribe({
      next: (res) => {
        this.appendHistory({ valid: res.valid, error: false, purchaseId: code, vehicle, checkedAt });
        this.result.set({ valid: res.valid, error: false, code, vehicle, checkedAt });
        this.busy.set(false);
        this.form.controls.code.reset('');
      },
      error: (err: HttpErrorResponse) => {
        const notFound = err.status === 404;
        this.appendHistory({ valid: notFound ? false : null, error: true, purchaseId: code, vehicle, checkedAt });
        this.result.set({ valid: notFound ? false : null, error: true, code, vehicle, checkedAt });
        this.busy.set(false);
        this.form.controls.code.reset('');
      },
    });
  }

  protected closeResult(): void {
    this.result.set(null);
  }

  private appendHistory(entry: Omit<ValidationEntry, 'id'>): void {
    this.history.update((prev) => [{ id: randomId(), ...entry }, ...prev].slice(0, 12));
  }
}
