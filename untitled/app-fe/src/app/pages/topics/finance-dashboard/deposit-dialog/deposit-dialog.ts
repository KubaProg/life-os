import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { FinanceStore } from '../../../../services/finance.store';
import { InvestmentDto } from '../../../../models/investment.model';

@Component({
  selector: 'app-deposit-dialog',
  imports: [ReactiveFormsModule, ButtonModule, DatePickerModule, DialogModule, InputNumberModule, InputTextModule, MessageModule, SelectModule],
  templateUrl: './deposit-dialog.html',
  styleUrl: './deposit-dialog.scss'
})
export class DepositDialog {
  protected readonly store = inject(FinanceStore);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly visible = signal(false);
  protected readonly error = signal<string | null>(null);
  readonly saved = output<string>();
  private requestId = '';
  protected readonly investment = signal<InvestmentDto | null>(null);
  protected readonly accounts = computed(() => this.store.accounts().filter(account => account.currency === this.investment()?.currency).map(account => ({
    ...account, label: `${account.name} · ${account.currency}`
  })));
  protected readonly today = new Date();
  protected readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(160)]),
    sourceAccountId: this.fb.control<number | null>(null, Validators.required),
    principal: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    annualRate: this.fb.control<number | null>(null, [Validators.required, Validators.min(0), Validators.max(100)]),
    taxRate: this.fb.control<number | null>(19, [Validators.required, Validators.min(0), Validators.max(100)]),
    startDate: this.fb.control<Date | null>(new Date(), Validators.required),
    maturityDate: this.fb.control<Date | null>(null, Validators.required)
  });

  open(investment: InvestmentDto): void {
    if (this.store.saving()) return;
    this.investment.set(investment);
    this.requestId = crypto.randomUUID();
    this.form.reset({ name: investment.name, sourceAccountId: this.accounts()[0]?.id ?? null, principal: null,
      annualRate: null, taxRate: 19, startDate: new Date(), maturityDate: null });
    this.error.set(null);
    this.visible.set(true);
  }

  protected close(visible: boolean): void {
    if (!this.store.saving()) this.visible.set(visible);
  }

  protected submit(): void {
    if (this.store.saving()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error.set('Uzupełnij nazwę, konto, kapitał, procenty i daty. Procenty muszą mieścić się w zakresie 0–100.');
      return;
    }
    const value = this.form.getRawValue();
    if (value.sourceAccountId === null || value.principal === null || value.annualRate === null ||
        value.taxRate === null || !value.startDate || !value.maturityDate) return;
    if (value.maturityDate <= value.startDate) {
      this.error.set('Koniec lokaty musi przypadać po dniu rozpoczęcia.');
      return;
    }
    this.error.set(null);
    const investment = this.investment();
    if (!investment) return;
    this.store.configureDeposit(investment.id, { requestId: this.requestId, name: investment.name, sourceAccountId: value.sourceAccountId,
      principal: value.principal, annualRate: value.annualRate, taxRate: value.taxRate,
      startDate: this.formatDate(value.startDate), maturityDate: this.formatDate(value.maturityDate)
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.visible.set(false);
        this.saved.emit('Warunki lokaty zostały zapisane, a kapitał przeniesiony z wybranego konta.');
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Nie udało się zapisać lokaty. Spróbuj ponownie.')
    });
  }

  private formatDate(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
