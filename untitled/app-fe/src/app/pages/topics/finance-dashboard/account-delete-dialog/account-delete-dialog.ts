import { HttpErrorResponse } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { FinanceStore } from '../../../../services/finance.store';
import { AccountReference } from '../../../../models/transfer.model';

type DeletableAccount = {
  id: number; name: string; kind: AccountReference['kind'];
  balance: number; currency: string;
};

@Component({
  selector: 'app-account-delete-dialog',
  imports: [ButtonModule, CurrencyPipe, DialogModule, FormsModule, MessageModule, SelectModule],
  templateUrl: './account-delete-dialog.html',
  styleUrl: './account-delete-dialog.scss'
})
export class AccountDeleteDialog {
  protected readonly store = inject(FinanceStore);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly selectedAccount = signal<DeletableAccount | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly destinationId = signal<number | null>(null);
  protected readonly destinations = computed(() => this.store.accounts()
    .filter(account => account.id !== this.selectedAccount()?.id && account.currency === this.selectedAccount()?.currency)
    .map(account => ({ id: account.id, label: `${account.name} · ${account.currency}` })));
  protected readonly canDelete = computed(() => {
    const account = this.selectedAccount();
    return !!account && account.balance >= 0
      && (account.balance === 0 || this.destinations().some(destination => destination.id === this.destinationId()));
  });

  open(account: DeletableAccount): void {
    if (this.store.saving()) return;
    this.error.set(null);
    this.destinationId.set(null);
    this.store.ensureOverviewLoaded();
    this.selectedAccount.set(account);
  }

  protected close(): void {
    if (!this.store.saving()) this.selectedAccount.set(null);
  }

  protected confirmDelete(): void {
    const account = this.selectedAccount();
    if (!account || this.store.saving() || !this.canDelete()) return;
    this.error.set(null);
    this.store.deleteAccount(account.id, account.kind, {
      destinationAccountId: account.balance > 0 ? this.destinationId() ?? undefined : undefined,
      expectedBalance: account.balance
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.selectedAccount.set(null);
        void this.router.navigate(['/finanse']);
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Nie udało się usunąć konta. Spróbuj ponownie.')
    });
  }
}
