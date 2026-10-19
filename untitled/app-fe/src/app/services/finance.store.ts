import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable, tap } from 'rxjs';

import {
  FinanceOverviewDto,
  TransactionCreatedResponse,
  TransactionUpdatedResponse,
  TransactionDeletedResponse
} from '../models/finance-overview.model';
import { TransactionRequest } from '../models/transaction.model';
import { TransferRequest, TransferResponse } from '../models/transfer.model';
import { DepositRequest, InvestmentMutationResponse } from '../models/investment.model';
import { InvestmentApiService } from './investment-api.service';
import { FinanceApiService } from './finance-api.service';

@Injectable({ providedIn: 'root' })
export class FinanceStore {
  private readonly financeApi = inject(FinanceApiService);
  private readonly investmentApi = inject(InvestmentApiService);

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
