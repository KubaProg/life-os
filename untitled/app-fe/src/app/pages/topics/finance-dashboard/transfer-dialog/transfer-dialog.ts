import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { AccountReference } from '../../../../models/transfer.model';
import { investmentAccountKind, investmentTypeLabel } from '../../../../models/investment.model';
import { FinanceStore } from '../../../../services/finance.store';
import { todayDate } from '../../../../utils/date';

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
  protected readonly fixedDestinationKey = signal<string | null>(null);
  readonly saved = output<string>();
  private requestId = '';
  protected readonly today = signal(todayDate());
  protected readonly endpoints = computed(() => [
    ...this.store.accounts().map(account => ({ key: `ORDINARY:${account.id}`,
      reference: { kind: 'ORDINARY', id: account.id } as AccountReference,
      label: `${account.name} · ${account.currency}`, currency: account.currency })),
    ...this.store.investments().filter(account => account.type !== 'DEPOSIT' || (account.status !== 'SETTLED' && account.status !== 'DRAFT')).map(account => ({ key: `${investmentAccountKind(account.type)}:${account.id}`,
      reference: { kind: investmentAccountKind(account.type), id: account.id } as AccountReference,
      label: `${account.name} · ${investmentTypeLabel(account.type)} · ${account.currency}`, currency: account.currency }))
  ]);
  protected readonly fixedDestination = computed(() =>
    this.endpoints().find(endpoint => endpoint.key === this.fixedDestinationKey()) ?? null);
  protected readonly sourceOptions = computed(() => {
    const destination = this.fixedDestination();
    return destination ? this.endpoints().filter(endpoint =>
      endpoint.key !== destination.key && endpoint.currency === destination.currency) : this.endpoints();
  });
  protected readonly form = this.fb.group({
    source: this.fb.control<string | null>(null, Validators.required),
    destination: this.fb.control<string | null>(null, Validators.required),
    amount: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    date: this.fb.control<Date | null>(todayDate(), Validators.required)
  });
  private readonly selectedSource = toSignal(this.form.controls.source.valueChanges, { initialValue: this.form.controls.source.value });
  private readonly selectedDestination = toSignal(this.form.controls.destination.valueChanges, { initialValue: this.form.controls.destination.value });
  protected readonly involvesDeposit = computed(() =>
    [this.selectedSource(), this.selectedDestination()].some(key => key?.startsWith('DEPOSIT:')));
  protected readonly involvesBondAccount = computed(() =>
    [this.selectedSource(), this.selectedDestination()].some(key => key?.startsWith('BOND:')));

  open(source: AccountReference | null = null, amount: number | null = null, destination: AccountReference | null = null): void {
    if (this.store.saving()) return;
    this.today.set(todayDate());
    this.store.ensureOverviewLoaded();
    this.requestId = crypto.randomUUID();
    this.fixedDestinationKey.set(destination ? `${destination.kind}:${destination.id}` : null);
    this.form.reset({ source: source ? `${source.kind}:${source.id}` : null,
      destination: destination ? `${destination.kind}:${destination.id}` : null, amount, date: todayDate() });
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
    const destination = this.endpoints().find(endpoint => endpoint.key === (this.fixedDestinationKey() ?? value.destination));
    if (!source || !destination) {
      this.error.set('Nie znaleziono rachunku źródłowego lub docelowego. Odśwież dane konta.');
      return;
    }
    if (value.amount === null || !value.date) return;
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
