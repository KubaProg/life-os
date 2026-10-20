import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, combineLatest, map, of, startWith, Subject, switchMap } from 'rxjs';
import { InvestmentDetailsDto, investmentOperationLabel, investmentStatusLabel, investmentTypeLabel, investmentRoute } from '../../../../models/investment.model';
import { InvestmentApiService } from '../../../../services/investment-api.service';
import { FinanceStore } from '../../../../services/finance.store';
import { TransferDialog } from '../transfer-dialog/transfer-dialog';
import { DepositDialog } from '../deposit-dialog/deposit-dialog';
import { AccountDeleteDialog } from '../account-delete-dialog/account-delete-dialog';

@Component({
  selector: 'app-deposit-details',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, RouterLink, ButtonModule, MessageModule, ProgressSpinnerModule, TransferDialog, DepositDialog, AccountDeleteDialog],
  templateUrl: './deposit-details.html',
  styleUrl: './deposit-details.scss'
})
export class DepositDetails {
  private readonly api = inject(InvestmentApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly store = inject(FinanceStore);
  protected readonly details = signal<InvestmentDetailsDto | null>(null);
  protected readonly state = signal<'loading' | 'ready' | 'not-found' | 'error'>('loading');
  protected readonly success = signal<string | null>(null);
  protected readonly operationLabel = investmentOperationLabel;
  protected readonly statusLabel = investmentStatusLabel;
  protected readonly typeLabel = investmentTypeLabel;
  private readonly reload = new Subject<void>();
  protected readonly chart = computed(() => {
    const values = this.details()?.valuations ?? [];
    if (values.length < 2) return null;
    const min = Math.min(...values.map(value => value.currentValue));
    const max = Math.max(...values.map(value => value.currentValue));
    const range = max - min || 1;
    // Coordinates only; all financial values and calculations come from the backend.
    const points = values.map((value, index) => `${10 + index / (values.length - 1) * 580},${145 - (value.currentValue - min) / range * 130}`).join(' ');
    return { points, first: values[0], last: values[values.length - 1] };
  });

  constructor() {
    this.store.refreshOverview();
    combineLatest([this.route.paramMap, this.reload.pipe(startWith(undefined))]).pipe(
      map(([params]) => Number(params.get('investmentId'))),
      switchMap(id => {
        this.state.set('loading');
        this.details.set(null);
        if (!Number.isInteger(id) || id <= 0) { this.state.set('not-found'); return of(null); }
        return this.api.getDetails(id).pipe(catchError((error: HttpErrorResponse) => {
          this.state.set(error.status === 404 ? 'not-found' : 'error');
          return of(null);
        }));
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(details => {
      if (details && details.investment.type !== 'DEPOSIT') {
        void this.router.navigate([investmentRoute(details.investment.type), details.investment.id], { replaceUrl: true });
      } else if (details) { this.details.set(details); this.state.set('ready'); }
    });
  }

  protected refresh(message?: string): void {
    if (message) this.success.set(message);
    this.reload.next();
  }
}
