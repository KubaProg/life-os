import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import {FinanceDataService} from '../../../services/finance-data.service';

@Component({
  selector: 'app-finance-dashboard',
  imports: [ButtonModule, CardModule, CurrencyPipe, DatePipe, DecimalPipe, RouterLink],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss'
})
export class FinanceDashboard {
  private readonly financeData = inject(FinanceDataService);

  protected readonly accounts = this.financeData.accounts;
  protected readonly transactions = this.financeData.transactions.slice(0, 4);
  protected readonly totalBalance = this.accounts.reduce((total, account) => total + account.balance, 0);
  protected readonly income = 9200;
  protected readonly expenses = 3240.82;
  protected readonly monthlyBalance = this.income - this.expenses;
}
