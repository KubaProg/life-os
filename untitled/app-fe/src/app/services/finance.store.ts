import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable, tap } from 'rxjs';

import {
  FinanceOverviewDto,
  TransactionCreatedResponse
} from '../models/finance-overview.model';
import { TransactionRequest } from '../models/transaction.model';
import { FinanceApiService } from './finance-api.service';

@Injectable({ providedIn: 'root' })
export class FinanceStore {
  private readonly financeApi = inject(FinanceApiService);

  private readonly overviewState = signal<FinanceOverviewDto | null>(null);
  private readonly loadingState = signal(false);
  private readonly savingState = signal(false);
  private readonly loadErrorState = signal(false);

  readonly overview = this.overviewState.asReadonly();
  readonly loading = this.loadingState.asReadonly();
  readonly saving = this.savingState.asReadonly();
  readonly loadError = this.loadErrorState.asReadonly();

  readonly accounts = computed(() => this.overviewState()?.accounts ?? []);
  readonly accountTotals = computed(() => this.overviewState()?.accountTotals ?? []);
  readonly transactions = computed(() => this.overviewState()?.transactions ?? []);

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

    return this.financeApi.createTransaction(request).pipe(
      tap((response) => {
        this.overviewState.set(response.overview);
        this.loadErrorState.set(false);
      }),
      finalize(() => this.savingState.set(false))
    );
  }

  private loadOverview(): void {
    this.loadingState.set(true);
    this.loadErrorState.set(false);

    this.financeApi
      .getOverview()
      .pipe(finalize(() => this.loadingState.set(false)))
      .subscribe({
        next: (overview) => this.overviewState.set(overview),
        error: () => {
          this.overviewState.set(null);
          this.loadErrorState.set(true);
        }
      });
  }
}
