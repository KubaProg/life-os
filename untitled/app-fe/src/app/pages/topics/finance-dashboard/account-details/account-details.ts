import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { map } from 'rxjs';
import {FinanceDataService} from '../../../../services/finance-data.service';

@Component({
  selector: 'app-account-details',
  imports: [ButtonModule, CurrencyPipe, DatePipe, RouterLink, TagModule],
  templateUrl: './account-details.html',
  styleUrl: './account-details.scss'
})
export class AccountDetails {
  private readonly route = inject(ActivatedRoute);
  private readonly financeData = inject(FinanceDataService);
  private readonly accountId = toSignal(
    this.route.paramMap.pipe(map((params) => params.get('accountId'))),
    { initialValue: null }
  );

  protected readonly account = computed(() => this.financeData.getAccount(this.accountId()));
  protected readonly transactions = computed(() => {
    const account = this.account();
    return account ? this.financeData.getTransactionsForAccount(account.id) : [];
  });
  protected readonly monthlyIncome = computed(() =>
    this.transactions().filter((transaction) => transaction.amount > 0).reduce((total, transaction) => total + transaction.amount, 0)
  );
  protected readonly monthlyExpenses = computed(() =>
    this.transactions().filter((transaction) => transaction.amount < 0).reduce((total, transaction) => total + Math.abs(transaction.amount), 0)
  );
}
