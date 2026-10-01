import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, map, of, switchMap } from 'rxjs';

import { AccountDto } from '../../../../models/account.model';
import { AccountApiService } from '../../../../services/account-api.service';
import { accountTypeColor, accountTypeLabel } from '../account-display';

type AccountLoadState = 'loading' | 'ready' | 'not-found' | 'error';

@Component({
  selector: 'app-account-details',
  imports: [
    ButtonModule,
    CurrencyPipe,
    DatePipe,
    MessageModule,
    ProgressSpinnerModule,
    RouterLink
  ],
  templateUrl: './account-details.html',
  styleUrl: './account-details.scss'
})
export class AccountDetails {
  private readonly route = inject(ActivatedRoute);
  private readonly accountApi = inject(AccountApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly account = signal<AccountDto | null>(null);
  protected readonly loadState = signal<AccountLoadState>('loading');
  protected readonly accountTypeColor = accountTypeColor;
  protected readonly accountTypeLabel = accountTypeLabel;

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('accountId'))),
        switchMap((accountId) => {
          this.account.set(null);
          this.loadState.set('loading');

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
      .subscribe((account) => {
        if (account) {
          this.account.set(account);
          this.loadState.set('ready');
        }
      });
  }
}
