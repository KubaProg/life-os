import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { map } from 'rxjs';
import { OrdinaryAccountType } from '../../../../models/account.model';
import { InvestmentType, investmentRoute } from '../../../../models/investment.model';
import { FinanceStore } from '../../../../services/finance.store';

@Component({
  selector: 'app-account-create-dialog',
  imports: [ReactiveFormsModule, ButtonModule, DialogModule, InputTextModule, MessageModule, SelectModule],
  templateUrl: './account-create-dialog.html',
  styleUrl: './account-create-dialog.scss'
})
export class AccountCreateDialog {
  protected readonly store = inject(FinanceStore);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly visible = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly kinds = [{ label: 'Zwykłe', value: 'ORDINARY' }, { label: 'Inwestycyjne', value: 'INVESTMENT' }];
  protected readonly accountTypes = [
    { label: 'Konto bankowe', value: 'BANK_ACCOUNT' },
    { label: 'Konto oszczędnościowe', value: 'SAVINGS' },
    { label: 'Gotówka', value: 'CASH' }
  ];
  protected readonly investmentTypes = [
    { label: 'Lokata', value: 'DEPOSIT' }, { label: 'Obligacje', value: 'BOND' },
    { label: 'ETF', value: 'ETF' }, { label: 'Inna inwestycja', value: 'OTHER' }
  ];
  protected readonly form = this.fb.nonNullable.group({
    kind: this.fb.nonNullable.control<'ORDINARY' | 'INVESTMENT'>('ORDINARY', Validators.required),
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(160)]],
    currency: ['PLN', [Validators.required, Validators.pattern(/^[A-Z]{3}$/)]],
    accountType: this.fb.nonNullable.control<OrdinaryAccountType>('BANK_ACCOUNT', Validators.required),
    investmentType: this.fb.nonNullable.control<InvestmentType>('DEPOSIT', Validators.required)
  });
  protected readonly kind = toSignal(this.form.controls.kind.valueChanges, { initialValue: this.form.controls.kind.value });
  protected readonly investmentType = toSignal(this.form.controls.investmentType.valueChanges, { initialValue: this.form.controls.investmentType.value });

  open(): void {
    if (this.store.saving()) return;
    this.form.reset({ kind: 'ORDINARY', name: '', currency: 'PLN', accountType: 'BANK_ACCOUNT', investmentType: 'DEPOSIT' });
    this.error.set(null);
    this.visible.set(true);
  }

  protected close(visible: boolean): void { if (!this.store.saving()) this.visible.set(visible); }

  protected submit(): void {
    if (this.store.saving()) return;
    this.form.controls.currency.setValue(this.form.controls.currency.value.trim().toUpperCase());
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error.set('Podaj nazwę, typ konta i trzyliterowy kod waluty, np. PLN.');
      return;
    }
    this.error.set(null);
    const value = this.form.getRawValue();
    const base = { name: value.name.trim(), currency: value.currency };
    const investment = value.kind === 'INVESTMENT';
    const request = investment
      ? this.store.createInvestmentAccount({ ...base, type: value.investmentType }).pipe(map(response => response.investmentAccountId))
      : this.store.createAccount({ ...base, type: value.accountType });
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: id => {
        this.visible.set(false);
        void this.router.navigate([investment ? investmentRoute(value.investmentType) : '/finanse/konta', id]);
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Nie udało się utworzyć konta. Spróbuj ponownie.')
    });
  }
}
