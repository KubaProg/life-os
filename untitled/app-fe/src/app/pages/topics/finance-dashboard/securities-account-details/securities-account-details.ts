import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, combineLatest, map, of, startWith, Subject, switchMap } from 'rxjs';
import { InvestmentDto, investmentAccountKind, investmentTypeLabel } from '../../../../models/investment.model';
import { FinanceStore } from '../../../../services/finance.store';
import { InvestmentApiService } from '../../../../services/investment-api.service';
import { AccountDeleteDialog } from '../account-delete-dialog/account-delete-dialog';
import { TransferDialog } from '../transfer-dialog/transfer-dialog';
import { TransferHistory } from '../transfer-history/transfer-history';
import { BondPositions } from '../bond-positions/bond-positions';

@Component({
  selector: 'app-securities-account-details',
  imports: [CurrencyPipe, RouterLink, ButtonModule, MessageModule, ProgressSpinnerModule, AccountDeleteDialog, TransferDialog, TransferHistory, BondPositions],
  templateUrl: './securities-account-details.html',
  styleUrl: './securities-account-details.scss'
})
export class SecuritiesAccountDetails {
  protected readonly store = inject(FinanceStore);
  private readonly api = inject(InvestmentApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly loadedAccount = signal<InvestmentDto | null>(null);
  private readonly reload = new Subject<void>();
  protected readonly state = signal<'loading' | 'ready' | 'not-found' | 'error'>('loading');
  protected readonly success = signal<string | null>(null);
  protected readonly typeLabel = investmentTypeLabel;
  protected readonly accountKind = investmentAccountKind;
  protected readonly account = computed(() => {
    const loaded = this.loadedAccount();
    return this.store.investments().find(account => account.id === loaded?.id) ?? loaded;
  });
  protected readonly transfers = computed(() => this.store.transfers().filter(transfer =>
    transfer.source.id === this.account()?.id || transfer.destination.id === this.account()?.id));

  constructor() {
    this.store.refreshOverview();
    combineLatest([this.route.paramMap, this.reload.pipe(startWith(undefined))]).pipe(
      map(([params]) => Number(params.get('investmentId'))),
      switchMap(id => {
        this.state.set('loading');
        this.loadedAccount.set(null);
        if (!Number.isInteger(id) || id <= 0) { this.state.set('not-found'); return of(null); }
        return this.api.getDetails(id).pipe(catchError((error: HttpErrorResponse) => {
          this.state.set(error.status === 404 ? 'not-found' : 'error');
          return of(null);
        }));
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(details => {
      if (details?.investment.type === 'DEPOSIT') {
        void this.router.navigate(['/finanse/lokaty', details.investment.id], { replaceUrl: true });
      } else if (details) {
        this.loadedAccount.set(details.investment);
        this.state.set('ready');
      }
    });
  }
  protected refresh(message?: string): void {
    if (message) this.success.set(message);
    this.store.refreshOverview();
    this.reload.next();
  }
}
