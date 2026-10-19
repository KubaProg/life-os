import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';

import { TransactionDto } from '../../../../models/transaction.model';
import { FinanceStore } from '../../../../services/finance.store';

@Component({
  selector: 'app-transaction-delete-dialog',
  imports: [ButtonModule, CurrencyPipe, DatePipe, DialogModule, MessageModule],
  templateUrl: './transaction-delete-dialog.html',
  styleUrl: './transaction-delete-dialog.scss'
})
export class TransactionDeleteDialog {
  protected readonly financeStore = inject(FinanceStore);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly selectedTransaction = signal<TransactionDto | null>(null);
  protected readonly deleteError = signal<string | null>(null);
  readonly deleted = output<string>();

  open(transaction: TransactionDto): void {
    if (this.financeStore.saving()) {
      return;
    }
    this.deleteError.set(null);
    this.selectedTransaction.set(transaction);
  }

  protected close(): void {
    if (!this.financeStore.saving()) {
      this.selectedTransaction.set(null);
    }
  }

  protected confirmDelete(): void {
    const transaction = this.selectedTransaction();
    if (!transaction || this.financeStore.saving()) {
      return;
    }
    this.deleteError.set(null);
    this.financeStore.deleteTransaction(transaction.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.selectedTransaction.set(null);
          this.deleted.emit('Transakcja została usunięta.');
        },
        error: (error: HttpErrorResponse) => {
          this.deleteError.set(error.error?.code === 'TRANSACTION_NOT_FOUND'
            ? 'Transakcja nie istnieje. Odśwież historię.'
            : 'Nie udało się usunąć transakcji. Spróbuj ponownie.');
        }
      });
  }
}
