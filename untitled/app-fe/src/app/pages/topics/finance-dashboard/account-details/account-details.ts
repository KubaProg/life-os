import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
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
import { AccountDeleteDialog } from '../account-delete-dialog/account-delete-dialog';
import { todayDate } from '../../../../utils/date';

type AccountLoadState = 'loading' | 'ready' | 'not-found' | 'error';

@Component({
  selector: 'app-account-details',
  imports: [
    TransferHistory,
    AccountDeleteDialog,
    ReactiveFormsModule,
    DatePickerModule,
    InputNumberModule,
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
  private readonly fb = inject(FormBuilder);
  protected readonly balanceError = signal<string | null>(null);
  protected readonly balanceForm = this.fb.group({
    openingBalance: this.fb.control<number | null>(0, Validators.required),
    balanceStartDate: this.fb.control<Date | null>(todayDate(), Validators.required)
  });

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
          this.balanceError.set(null);

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
          const [year, month, day] = details.account.balanceStartDate.split('-').map(Number);
          this.balanceForm.reset({ openingBalance: details.account.currentBalance, balanceStartDate: new Date(year, month - 1, day) });
          this.loadedTransactions.set(details.transactions);
          this.loadedTransfers.set(details.transfers);
          this.loadState.set('ready');
        }
      });
  }

  protected saveOpeningBalance(): void {
    if (this.financeStore.saving()) return;
    this.balanceForm.markAllAsTouched();
    const value = this.balanceForm.getRawValue();
    const account = this.account();
    if (this.balanceForm.invalid || !account || value.openingBalance === null || !value.balanceStartDate) {
      this.balanceError.set('Podaj saldo początkowe i datę.');
      return;
    }
    const date = value.balanceStartDate;
    this.balanceError.set(null);
    this.successMessage.set(null);
    this.financeStore.configureOpeningBalance(account.id, {
      openingBalance: value.openingBalance,
      balanceStartDate: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: account => {
        this.loadedAccount.set(account);
        this.successMessage.set('Saldo początkowe zostało zapisane.');
      },
      error: (error: HttpErrorResponse) => this.balanceError.set(error.error?.message ?? 'Nie udało się zapisać salda początkowego.')
    });
  }
}
