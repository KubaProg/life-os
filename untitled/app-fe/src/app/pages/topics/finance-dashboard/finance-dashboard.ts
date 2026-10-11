import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';

import { TransactionRequest, TransactionType } from '../../../models/transaction.model';
import { FinanceStore } from '../../../services/finance.store';
import { accountTypeColor, accountTypeLabel } from './account-display';

@Component({
  selector: 'app-finance-dashboard',
  imports: [
    ButtonModule,
    CurrencyPipe,
    DatePipe,
    DatePickerModule,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    ProgressSpinnerModule,
    ReactiveFormsModule,
    RouterLink,
    SelectModule,
    TextareaModule
  ],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss'
})
export class FinanceDashboard {
  protected readonly financeStore = inject(FinanceStore);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly accountTypeColor = accountTypeColor;
  protected readonly accountTypeLabel = accountTypeLabel;
  protected readonly transactionDialogVisible = signal(false);
  protected readonly saveError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly today = new Date();
  protected readonly accountOptions = computed(() => [...this.financeStore.accounts()]);
  protected readonly transactionTypes: Array<{
    label: string;
    value: TransactionType;
  }> = [
    { label: 'Wydatek', value: 'EXPENSE' },
    { label: 'Przychód', value: 'INCOME' }
  ];

  protected readonly transactionForm = this.formBuilder.group({
    accountId: this.formBuilder.control<number | null>(null, Validators.required),
    type: this.formBuilder.control<TransactionType | null>('EXPENSE', Validators.required),
    amount: this.formBuilder.control<number | null>(null, [
      Validators.required,
      Validators.min(0.0001)
    ]),
    transactionDate: this.formBuilder.control<Date | null>(new Date(), Validators.required),
    category: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100)
    ]),
    description: this.formBuilder.nonNullable.control('', Validators.maxLength(500))
  });

  constructor() {
    this.financeStore.ensureOverviewLoaded();
  }

  protected openTransactionDialog(): void {
    const firstAccount = this.financeStore.accounts()[0];
    if (!firstAccount) {
      return;
    }

    this.transactionForm.reset({
      accountId: firstAccount.id,
      type: 'EXPENSE',
      amount: null,
      transactionDate: new Date(),
      category: '',
      description: ''
    });
    this.saveError.set(null);
    this.successMessage.set(null);
    this.transactionDialogVisible.set(true);
  }

  protected setTransactionDialogVisible(visible: boolean): void {
    if (!this.financeStore.saving()) {
      this.transactionDialogVisible.set(visible);
    }
  }

  protected submitTransaction(): void {
    if (this.financeStore.saving()) {
      return;
    }

    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    const value = this.transactionForm.getRawValue();
    if (
      value.accountId === null ||
      value.type === null ||
      value.amount === null ||
      value.transactionDate === null
    ) {
      return;
    }

    const request: TransactionRequest = {
      accountId: value.accountId,
      type: value.type,
      amount: value.amount,
      transactionDate: this.formatLocalDate(value.transactionDate),
      category: value.category.trim(),
      description: value.description.trim() || undefined
    };

    this.saveError.set(null);
    this.financeStore
      .createTransaction(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          const label = response.createdTransaction.type === 'INCOME' ? 'Przychód' : 'Wydatek';
          this.transactionDialogVisible.set(false);
          this.successMessage.set(`${label} został zapisany.`);
        },
        error: (error: HttpErrorResponse) => {
          this.saveError.set(this.getSaveErrorMessage(error));
        }
      });
  }

  private getSaveErrorMessage(error: HttpErrorResponse): string {
    switch (error.error?.code) {
      case 'TRANSACTION_BEFORE_BALANCE_START_DATE':
        return 'Data transakcji nie może być wcześniejsza niż data salda początkowego konta.';
      case 'ACCOUNT_NOT_FOUND':
        return 'Wybrane konto nie istnieje albo nie jest już aktywne.';
      case 'VALIDATION_FAILED':
        return 'Sprawdź poprawność wszystkich pól formularza.';
      default:
        return 'Nie udało się zapisać transakcji. Spróbuj ponownie.';
    }
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
