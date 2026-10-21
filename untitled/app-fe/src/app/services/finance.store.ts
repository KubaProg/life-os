import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, finalize, Observable, of, switchMap, map, tap } from 'rxjs';

import {
  FinanceOverviewDto,
  TransactionCreatedResponse,
  TransactionUpdatedResponse,
  TransactionDeletedResponse
} from '../models/finance-overview.model';
import { TransactionRequest } from '../models/transaction.model';
import { AccountReference, TransferRequest, TransferResponse } from '../models/transfer.model';
import { DepositRequest, InvestmentAccountRequest, InvestmentMutationResponse } from '../models/investment.model';
import { AccountDeletedResponse, AccountDto, AccountRequest, DeleteAccountRequest, OpeningBalanceRequest } from '../models/account.model';
import { AccountApiService } from './account-api.service';
import { InvestmentApiService } from './investment-api.service';
import { FinanceApiService } from './finance-api.service';
import { BondApiService } from './bond-api.service';
import { BondPurchaseRequest, BondRedemptionRequest } from '../models/bond.model';

@Injectable({ providedIn: 'root' })
export class FinanceStore {
  private readonly financeApi = inject(FinanceApiService);
  private readonly investmentApi = inject(InvestmentApiService);
  private readonly accountApi = inject(AccountApiService);
  private readonly bondApi = inject(BondApiService);

  private readonly overviewState = signal<FinanceOverviewDto | null>(null);
  private readonly loadingState = signal(false);
  private readonly savingState = signal(false);
  private readonly loadErrorState = signal(false);
  private snapshotVersion = 0;
  private loadRequestId = 0;
  private readonly summaryMonthState = signal<string | undefined>(undefined);
  readonly summaryMonth = this.summaryMonthState.asReadonly();

  readonly overview = this.overviewState.asReadonly();
  readonly loading = this.loadingState.asReadonly();
  readonly saving = this.savingState.asReadonly();
  readonly loadError = this.loadErrorState.asReadonly();

  readonly accounts = computed(() => this.overviewState()?.accounts ?? []);
  readonly investments = computed(() => this.overviewState()?.investments ?? []);
  readonly transfers = computed(() => this.overviewState()?.transfers ?? []);
  readonly accountTotals = computed(() => this.overviewState()?.accountTotals ?? []);
  readonly transactions = computed(() => this.overviewState()?.transactions ?? []);

  readonly investmentProfit = computed(() => {
    const profit = this.overviewState()?.investmentProfit;
    return profit && !this.loadingState() ? profit : null;
  });
  readonly monthlySummary = computed(() => {
    const summary = this.overviewState()?.monthlySummary;
    return summary && (!this.summaryMonthState() || summary.month === this.summaryMonthState()) ? summary : null;
  });

  selectSummaryMonth(month: string): void {
    this.summaryMonthState.set(month);
    this.loadOverview();
  }

  ensureOverviewLoaded(): void {
    if (this.overviewState() || this.loadingState()) {
      return;
    }

    this.loadOverview();
  }

  refreshOverview(): void {
    if (!this.loadingState()) {
      this.loadOverview();
    }
  }

  createTransaction(request: TransactionRequest): Observable<TransactionCreatedResponse> {
    this.savingState.set(true);

    return this.financeApi.createTransaction(request, this.summaryMonthState()).pipe(
      tap((response) => {
        this.snapshotVersion++;
        this.overviewState.set(response.overview);
        this.loadErrorState.set(false);
        if (this.summaryMonthState() && response.overview.monthlySummary.month !== this.summaryMonthState()) {
          this.loadOverview();
        }
      }),
      finalize(() => this.savingState.set(false))
    );
  }

  updateTransaction(id: number, request: TransactionRequest): Observable<TransactionUpdatedResponse> {
    this.savingState.set(true);
    return this.financeApi.updateTransaction(id, request, this.summaryMonthState()).pipe(
      tap((response) => {
        this.snapshotVersion++;
        this.overviewState.set(response.overview);
        this.loadErrorState.set(false);
        if (this.summaryMonthState() && response.overview.monthlySummary.month !== this.summaryMonthState()) {
          this.loadOverview();
        }
      }),
      finalize(() => this.savingState.set(false))
    );
  }

  deleteTransaction(id: number): Observable<TransactionDeletedResponse> {
    this.savingState.set(true);
    return this.financeApi.deleteTransaction(id, this.summaryMonthState()).pipe(
      tap((response) => {
        this.snapshotVersion++;
        this.overviewState.set(response.overview);
        this.loadErrorState.set(false);
        if (this.summaryMonthState() && response.overview.monthlySummary.month !== this.summaryMonthState()) {
          this.loadOverview();
        }
      }),
      finalize(() => this.savingState.set(false))
    );
  }

  createDeposit(request: DepositRequest): Observable<InvestmentMutationResponse> {
    this.savingState.set(true);
    return this.investmentApi.createDeposit(request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  createAccount(request: AccountRequest): Observable<number> {
    return this.mutateOrdinaryAccount(this.accountApi.createAccount(request)).pipe(map(account => account.id));
  }

  deleteAccount(id: number, kind: AccountReference['kind'], body: DeleteAccountRequest): Observable<AccountDeletedResponse> {
    this.savingState.set(true);
    const request = kind !== 'ORDINARY'
      ? this.investmentApi.deleteAccount(id, body, this.summaryMonthState())
      : this.accountApi.deleteAccount(id, body, this.summaryMonthState());
    return request.pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  configureOpeningBalance(id: number, request: OpeningBalanceRequest): Observable<AccountDto> {
    return this.mutateOrdinaryAccount(this.accountApi.configureOpeningBalance(id, request));
  }

  private mutateOrdinaryAccount(mutation: Observable<AccountDto>): Observable<AccountDto> {
    this.savingState.set(true);
    return mutation.pipe(
      // Invalidate older reads as soon as the account exists, including if the refresh fails.
      tap(() => this.snapshotVersion++),
      switchMap(account => this.financeApi.getOverview(this.summaryMonthState()).pipe(
        tap(overview => this.applyMutationSnapshot(overview)),
        map(() => account),
        // Creation succeeded: still open its details if the overview refresh is unavailable.
        catchError(() => { this.overviewState.set(null); this.loadErrorState.set(true); return of(account); })
      )),
      finalize(() => this.savingState.set(false))
    );
  }

  createInvestmentAccount(request: InvestmentAccountRequest): Observable<InvestmentMutationResponse> {
    this.savingState.set(true);
    return this.investmentApi.createAccount(request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  configureDeposit(id: number, request: DepositRequest): Observable<InvestmentMutationResponse> {
    this.savingState.set(true);
    return this.investmentApi.configureDeposit(id, request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  transfer(request: TransferRequest): Observable<TransferResponse> {
    this.savingState.set(true);
    return this.financeApi.transfer(request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  private applyMutationSnapshot(overview: FinanceOverviewDto): void {
    this.snapshotVersion++;
    this.overviewState.set(overview);
    this.loadErrorState.set(false);
    if (this.summaryMonthState() && overview.monthlySummary.month !== this.summaryMonthState()) {
      this.loadOverview();
    }
  }

  purchaseBonds(accountId: number, request: BondPurchaseRequest): Observable<InvestmentMutationResponse> {
    this.savingState.set(true);
    return this.bondApi.purchase(accountId, request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  redeemBonds(accountId: number, bondId: number, request: BondRedemptionRequest): Observable<InvestmentMutationResponse> {
    this.savingState.set(true);
    return this.bondApi.redeem(accountId, bondId, request, this.summaryMonthState()).pipe(
      tap(response => this.applyMutationSnapshot(response.overview)),
      finalize(() => this.savingState.set(false))
    );
  }

  private loadOverview(): void {
    const version = this.snapshotVersion;
    const requestId = ++this.loadRequestId;
    this.loadingState.set(true);
    this.loadErrorState.set(false);

    this.financeApi
      .getOverview(this.summaryMonthState())
      .pipe(finalize(() => {
        if (requestId === this.loadRequestId) this.loadingState.set(false);
      }))
      .subscribe({
        next: (overview) => {
          if (version === this.snapshotVersion && requestId === this.loadRequestId) {
            this.overviewState.set(overview);
          }
        },
        error: () => {
          if (version !== this.snapshotVersion || requestId !== this.loadRequestId) {
            return;
          }
          this.overviewState.set(null);
          this.loadErrorState.set(true);
        }
      });
  }
}
