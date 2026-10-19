import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, map, of, switchMap } from 'rxjs';

import { AccountDto } from '../../../../models/account.model';
import { TransferDto } from '../../../../models/transfer.model';
import { TransferHistory } from '../transfer-history/transfer-history';
import { TransactionDto } from '../../../../models/transaction.model';
import { AccountApiService } from '../../../../services/account-api.service';
import { FinanceStore } from '../../../../services/finance.store';
import { TransactionDeleteDialog } from '../transaction-delete-dialog/transaction-delete-dialog';
import { TransactionHistoryFilters } from '../transaction-history-filters/transaction-history-filters';
import { TransactionDialog } from '../transaction-dialog/transaction-dialog';
import { accountTypeColor, accountTypeLabel } from '../account-display';

type AccountLoadState = 'loading' | 'ready' | 'not-found' | 'error';

@Component({
  selector: 'app-account-details',
  imports: [
    TransferHistory,
    ButtonModule,
    CurrencyPipe,
    DatePipe,
    MessageModule,
    ProgressSpinnerModule,
    RouterLink,
    TransactionDialog,
    TransactionDeleteDialog,
    TransactionHistoryFilters
  ],
  templateUrl: './account-details.html',
  styleUrl: './account-details.scss'
})
export class AccountDetails {
  private readonly route = inject(ActivatedRoute);
  private readonly accountApi = inject(AccountApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly financeStore = inject(FinanceStore);
  private readonly loadedAccount = signal<AccountDto | null>(null);
  private readonly loadedTransactions = signal<ReadonlyArray<TransactionDto>>([]);
  private readonly loadedTransfers = signal<ReadonlyArray<TransferDto>>([]);
  protected readonly transfers = computed(() => {
    const overview = this.financeStore.overview();
    const id = this.loadedAccount()?.id;
    return overview ? overview.transfers.filter(transfer =>
      transfer.source.id === id || transfer.destination.id === id) : this.loadedTransfers();
  });
  protected readonly successMessage = signal<string | null>(null);
  protected readonly account = computed(() => {
    const loaded = this.loadedAccount();
    return this.financeStore.accounts().find((account) => account.id === loaded?.id) ?? loaded;
  });
  protected readonly transactions = computed(() => {
    const overview = this.financeStore.overview();
    return overview
      ? overview.transactions.filter((transaction) => transaction.accountId === this.loadedAccount()?.id)
      : this.loadedTransactions();
  });
  protected readonly loadState = signal<AccountLoadState>('loading');
  protected readonly accountTypeColor = accountTypeColor;
  protected readonly accountTypeLabel = accountTypeLabel;

  constructor() {
    this.financeStore.ensureOverviewLoaded();
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('accountId'))),
        switchMap((accountId) => {
          this.loadedAccount.set(null);
          this.loadedTransactions.set([]);
          this.loadedTransfers.set([]);
          this.loadState.set('loading');
          this.successMessage.set(null);

          if (!Number.isInteger(accountId) || accountId <= 0) {
            this.loadState.set('not-found');
            return of(null);
          }

          return this.accountApi.getAccount(accountId).pipe(
            catchError((error: HttpErrorResponse) => {
              this.loadState.set(error.status === 404 ? 'not-found' : 'error');
              return of(null);
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((details) => {
        if (details) {
          this.loadedAccount.set(details.account);
          this.loadedTransactions.set(details.transactions);
          this.loadedTransfers.set(details.transfers);
          this.loadState.set('ready');
        }
      });
  }
}
