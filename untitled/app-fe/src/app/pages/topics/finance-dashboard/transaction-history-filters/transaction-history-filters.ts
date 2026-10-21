import { Component, computed, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { FieldsetModule } from 'primeng/fieldset';
import { SelectModule } from 'primeng/select';

import { AccountDto } from '../../../../models/account.model';
import { TransactionDto, TransactionType } from '../../../../models/transaction.model';
import { todayDate } from '../../../../utils/date';

@Component({
  selector: 'app-transaction-history-filters',
  imports: [FormsModule, ButtonModule, DatePickerModule, FieldsetModule, SelectModule],
  templateUrl: './transaction-history-filters.html',
  styleUrl: './transaction-history-filters.scss'
})
export class TransactionHistoryFilters {
  readonly transactions = input<ReadonlyArray<TransactionDto>>([]);
  readonly accounts = input<ReadonlyArray<AccountDto>>([]);
  readonly showAccountFilter = input(false);
  readonly disabled = input(false);
  readonly idPrefix = input.required<string>();
  protected readonly collapsed = signal(true);

  protected readonly selectedMonth = signal<Date | null>(todayDate());
  protected readonly selectedType = signal<TransactionType | null>(null);
  protected readonly selectedAccountId = signal<number | null>(null);
  protected readonly typeOptions = [
    { label: 'Wszystkie', value: null },
    { label: 'Przychody', value: 'INCOME' },
    { label: 'Wydatki', value: 'EXPENSE' }
  ];
  protected readonly accountOptions = computed(() => [
    { label: 'Wszystkie konta', value: null },
    ...this.accounts().map((account) => ({
      label: `${account.name} · ${account.currency}`,
      value: account.id
    }))
  ]);
  protected readonly activeFilterCount = computed(() =>
    Number(this.selectedMonth() !== null) + Number(this.selectedType() !== null) +
    Number(this.showAccountFilter() && this.selectedAccountId() !== null)
  );
  protected readonly hasActiveFilters = computed(() => this.activeFilterCount() > 0);

  readonly filteredTransactions = computed(() => {
    const date = this.selectedMonth();
    const month = date
      ? `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      : null;
    const type = this.selectedType();
    const accountId = this.showAccountFilter() ? this.selectedAccountId() : null;
    return this.transactions().filter((transaction) =>
      (month === null || transaction.transactionDate.startsWith(`${month}-`)) &&
      (type === null || transaction.type === type) &&
      (accountId === null || transaction.accountId === accountId)
    );
  });

  protected reset(): void {
    this.selectedMonth.set(null);
    this.selectedType.set(null);
    this.selectedAccountId.set(null);
  }
}
