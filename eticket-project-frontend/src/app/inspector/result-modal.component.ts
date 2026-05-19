import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  input,
  output,
} from '@angular/core';
import { DatePipe } from '@angular/common';

export interface ValidationResult {
  valid: boolean | null;
  error: boolean;
  code: string;
  vehicle: string;
}

@Component({
  selector: 'app-result-modal',
  imports: [DatePipe],
  templateUrl: './result-modal.component.html',
  styleUrl: './result-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResultModalComponent {
  readonly result = input.required<ValidationResult>();
  readonly close = output<void>();

  @HostListener('document:keydown.escape')
  protected onEsc(): void {
    this.close.emit();
  }
}
