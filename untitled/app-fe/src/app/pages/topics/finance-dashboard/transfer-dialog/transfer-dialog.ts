import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { AccountReference } from '../../../../models/transfer.model';
import { FinanceStore } from '../../../../services/finance.store';

@Component({
  selector: 'app-transfer-dialog',
  imports: [ReactiveFormsModule, ButtonModule, DatePickerModule, DialogModule, InputNumberModule, MessageModule, SelectModule],
  templateUrl: './transfer-dialog.html',
  styleUrl: './transfer-dialog.scss'
})
export class TransferDialog {
  protected readonly store = inject(FinanceStore);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly visible = signal(false);
  protected readonly error = signal<string | null>(null);
  readonly saved = output<string>();
  private requestId = '';
  protected readonly today = new Date();
  protected readonly endpoints = computed(() => [
    ...this.store.accounts().map(account => ({ key: `ORDINARY:${account.id}`,
      reference: { kind: 'ORDINARY', id: account.id } as AccountReference,
      label: `${account.name} · ${account.currency}`, currency: account.currency })),
    ...this.store.investments().filter(account => account.status !== 'SETTLED').map(account => ({ key: `INVESTMENT:${account.id}`,
      reference: { kind: 'INVESTMENT', id: account.id } as AccountReference,
      label: `${account.name} · lokata · ${account.currency}`, currency: account.currency }))
  ]);
  protected readonly form = this.fb.group({
    source: this.fb.control<string | null>(null, Validators.required),
    destination: this.fb.control<string | null>(null, Validators.required),
    amount: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    date: this.fb.control<Date | null>(new Date(), Validators.required)
  });

  open(source: AccountReference | null = null, amount: number | null = null): void {
    if (this.store.saving()) return;
    this.store.ensureOverviewLoaded();
    this.requestId = crypto.randomUUID();
    this.form.reset({ source: source ? `${source.kind}:${source.id}` : null, destination: null, amount, date: new Date() });
    this.error.set(null);
    this.visible.set(true);
  }

  protected close(visible: boolean): void { if (!this.store.saving()) this.visible.set(visible); }

  protected submit(): void {
    if (this.store.saving()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) { this.error.set('Wybierz źródło, cel, kwotę i datę.'); return; }
    const value = this.form.getRawValue();
    const source = this.endpoints().find(endpoint => endpoint.key === value.source);
    const destination = this.endpoints().find(endpoint => endpoint.key === value.destination);
    if (!source || !destination || value.amount === null || !value.date) return;
    if (source.key === destination.key || source.currency !== destination.currency) {
      this.error.set('Wybierz różne rachunki w tej samej walucie.'); return;
    }
    const date = value.date;
    this.error.set(null);
    this.store.transfer({ requestId: this.requestId, sourceAccountId: source.reference.id, destinationAccountId: destination.reference.id, amount: value.amount,
      date: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.visible.set(false);
        this.saved.emit('Transfer został zapisany. Salda i podsumowanie zostały odświeżone.');
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Nie udało się zapisać transferu. Możesz ponowić tę samą operację.')
    });
  }
}
