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
import { TextareaModule } from 'primeng/textarea';
import { Observable } from 'rxjs';

import { TransactionCreatedResponse, TransactionUpdatedResponse } from '../../../../models/finance-overview.model';
import { TransactionDto, TransactionRequest, TransactionType } from '../../../../models/transaction.model';
import { FinanceStore } from '../../../../services/finance.store';

@Component({
  selector: 'app-transaction-dialog',
  imports: [
    ButtonModule,
    DatePickerModule,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    ReactiveFormsModule,
    SelectModule,
    TextareaModule
  ],
  templateUrl: './transaction-dialog.html',
  styleUrl: './transaction-dialog.scss'
})
export class TransactionDialog {
  protected readonly financeStore = inject(FinanceStore);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly transactionDialogVisible = signal(false);
  protected readonly saveError = signal<string | null>(null);
  readonly saved = output<string>();
  protected readonly editingTransaction = signal<TransactionDto | null>(null);
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

  open(transaction: TransactionDto | null = null): void {
    if (this.financeStore.saving()) {
      return;
    }
    const accountId = transaction?.accountId ?? this.financeStore.accounts()[0]?.id;
    if (accountId === undefined) {
      return;
    }
    this.editingTransaction.set(transaction);
    const date = transaction?.transactionDate;
    const parts = date?.split('-').map(Number);
    this.transactionForm.reset({
      accountId,
      type: transaction?.type ?? 'EXPENSE',
      amount: transaction?.amount ?? null,
      transactionDate: parts ? new Date(parts[0], parts[1] - 1, parts[2]) : new Date(),
      category: transaction?.category ?? '',
      description: transaction?.description ?? ''
    });
    this.saveError.set(null);
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
    const editing = this.editingTransaction();
    const saveRequest: Observable<TransactionCreatedResponse | TransactionUpdatedResponse> = editing
      ? this.financeStore.updateTransaction(editing.id, request)
      : this.financeStore.createTransaction(request);
    saveRequest
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const label = request.type === 'INCOME' ? 'Przychód' : 'Wydatek';
          this.transactionDialogVisible.set(false);
          this.saved.emit(editing ? 'Transakcja została zaktualizowana.' : `${label} został zapisany.`);
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
      case 'TRANSACTION_NOT_FOUND':
        return 'Transakcja nie istnieje. Odśwież historię i spróbuj ponownie.';
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
