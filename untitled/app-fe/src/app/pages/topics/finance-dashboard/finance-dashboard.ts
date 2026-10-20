import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { investmentTypeLabel, investmentStatusLabel, investmentRoute } from '../../../models/investment.model';
import { FinanceStore } from '../../../services/finance.store';
import { accountTypeColor, accountTypeLabel } from './account-display';
import { TransactionDeleteDialog } from './transaction-delete-dialog/transaction-delete-dialog';
import { TransactionHistoryFilters } from './transaction-history-filters/transaction-history-filters';
import { AccountCreateDialog } from './account-create-dialog/account-create-dialog';
import { TransferDialog } from './transfer-dialog/transfer-dialog';
import { TransferHistory } from './transfer-history/transfer-history';
import { TransactionDialog } from './transaction-dialog/transaction-dialog';

@Component({
  selector: 'app-finance-dashboard',
  imports: [AccountCreateDialog, TransferDialog, TransferHistory, FormsModule, DatePickerModule, DecimalPipe, ButtonModule, CurrencyPipe, DatePipe, MessageModule, ProgressSpinnerModule, RouterLink, TransactionDialog, TransactionDeleteDialog, TransactionHistoryFilters],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss'
})
export class FinanceDashboard {
  protected readonly financeStore = inject(FinanceStore);
  protected readonly accountTypeColor = accountTypeColor;
  protected readonly accountTypeLabel = accountTypeLabel;
  protected readonly investmentTypeLabel = investmentTypeLabel;
  protected readonly investmentStatusLabel = investmentStatusLabel;
  protected readonly investmentRoute = investmentRoute;
  protected readonly successMessage = signal<string | null>(null);

  protected readonly selectedMonth = signal(this.initialMonth());
  protected readonly metrics = [
    { key: 'income', label: 'Przychody' },
    { key: 'expenses', label: 'Wydatki' },
    { key: 'savingsRate', label: 'Stopa oszczędzania' }
  ] as const;

  private initialMonth(): Date {
    const month = this.financeStore.summaryMonth() ?? this.financeStore.monthlySummary()?.month
      ?? new Intl.DateTimeFormat('sv-SE', { timeZone: 'Europe/Warsaw', year: 'numeric', month: '2-digit' }).format(new Date());
    const [year, monthNumber] = month.split('-').map(Number);
    return new Date(year, monthNumber - 1, 1);
  }

  protected changeMonth(date: Date | null): void {
    if (!date) return;
    this.selectedMonth.set(date);
    this.financeStore.selectSummaryMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`);
  }

  constructor() {
    // Refresh on every dashboard entry, including returns from account details.
    this.financeStore.refreshOverview();
  }
}
