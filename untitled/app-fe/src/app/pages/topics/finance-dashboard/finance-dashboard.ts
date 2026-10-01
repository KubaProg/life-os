import { CurrencyPipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { finalize } from 'rxjs';

import { AccountDto } from '../../../models/account.model';
import { AccountApiService } from '../../../services/account-api.service';
import { accountTypeColor, accountTypeLabel } from './account-display';

@Component({
  selector: 'app-finance-dashboard',
  imports: [ButtonModule, CurrencyPipe, MessageModule, ProgressSpinnerModule, RouterLink],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss'
})
export class FinanceDashboard {
  private readonly accountApi = inject(AccountApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly accounts = signal<ReadonlyArray<AccountDto>>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal(false);
  protected readonly accountTypeColor = accountTypeColor;
  protected readonly accountTypeLabel = accountTypeLabel;
  protected readonly balancesByCurrency = computed(() => {
    const totals = new Map<string, number>();

    for (const account of this.accounts()) {
      totals.set(account.currency, (totals.get(account.currency) ?? 0) + account.currentBalance);
    }

    return Array.from(totals, ([currency, total]) => ({ currency, total })).sort((a, b) =>
      a.currency.localeCompare(b.currency)
    );
  });

  constructor() {
    this.loadAccounts();
  }

  protected loadAccounts(): void {
    this.loading.set(true);
    this.loadError.set(false);

    this.accountApi
      .getAccounts()
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (accounts) => this.accounts.set(accounts),
        error: () => {
          this.accounts.set([]);
          this.loadError.set(true);
        }
      });
  }

}
