import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, input, OnInit, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { finalize, forkJoin } from 'rxjs';
import { BondDetailsDto, BondIssueDto, BondPositionDto, BondPurchaseQuote, BondRedemptionQuote, bondOperationLabel } from '../../../../models/bond.model';
import { BondApiService } from '../../../../services/bond-api.service';
import { FinanceStore } from '../../../../services/finance.store';
import { todayDate } from '../../../../utils/date';

@Component({
  selector: 'app-bond-positions',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, ReactiveFormsModule, ButtonModule, DatePickerModule,
    DialogModule, InputNumberModule, MessageModule, SelectModule],
  templateUrl: './bond-positions.html',
  styleUrl: './bond-positions.scss'
})
export class BondPositions implements OnInit {
  readonly accountId = input.required<number>();
  readonly currency = input.required<string>();
  readonly saved = output<string>();
  protected readonly store = inject(FinanceStore);
  private readonly api = inject(BondApiService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly details = signal<BondDetailsDto | null>(null);
  protected readonly issues = signal<BondIssueDto[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly mode = signal<'purchase' | 'redemption' | null>(null);
  protected readonly selectedPosition = signal<BondPositionDto | null>(null);
  protected readonly purchaseQuote = signal<BondPurchaseQuote | null>(null);
  protected readonly redemptionQuote = signal<BondRedemptionQuote | null>(null);
  protected readonly quoting = signal(false);
  protected readonly today = signal(todayDate());
  protected readonly operationLabel = bondOperationLabel;
  private requestId = '';
  private quoteVersion = 0;
  protected readonly form = this.fb.group({
    issueId: this.fb.control<number | null>(null),
    quantity: this.fb.control<number | null>(1, [Validators.required, Validators.min(1), Validators.max(1000000), Validators.pattern(/^\d+$/)]),
    date: this.fb.control<Date | null>(todayDate(), Validators.required)
  });

  constructor() {
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.quoteVersion++;
      this.purchaseQuote.set(null); this.redemptionQuote.set(null); this.formError.set(null);
    });
  }
  ngOnInit(): void { this.load(); }
  protected load(): void {
    this.loading.set(true); this.error.set(null);
    forkJoin({ details: this.api.details(this.accountId()), issues: this.api.issues() })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))).subscribe({
        next: value => { this.details.set(value.details); this.issues.set(value.issues); },
        error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Nie udało się pobrać obligacji.')
      });
  }
  protected openPurchase(): void {
    if (this.store.saving()) return;
    this.today.set(todayDate());
    this.selectedPosition.set(null); this.mode.set('purchase'); this.requestId = crypto.randomUUID();
    const date = this.formatDate(todayDate());
    const issue = this.issues().find(i => i.saleFrom <= date && i.saleUntil >= date) ?? this.issues()[0];
    this.form.reset({ issueId: issue?.id ?? null, quantity: 1, date: todayDate() });
    this.formError.set(null);
  }
  protected openRedemption(position: BondPositionDto): void {
    if (this.store.saving()) return;
    this.today.set(todayDate());
    this.selectedPosition.set(position); this.mode.set('redemption'); this.requestId = crypto.randomUUID();
    this.form.reset({ issueId: null, quantity: 1, date: todayDate() });
    this.formError.set(null);
  }
  protected close(visible: boolean): void {
    if (!visible && !this.store.saving() && !this.quoting()) {
      this.quoteVersion++; this.mode.set(null); this.purchaseQuote.set(null); this.redemptionQuote.set(null);
    }
  }
  protected preview(): void {
    if (this.quoting() || this.store.saving()) return;
    this.form.markAllAsTouched();
    const value = this.form.getRawValue();
    if (this.form.invalid || !value.date || value.quantity === null) {
      this.formError.set('Podaj pełną liczbę sztuk i datę.'); return;
    }
    const version = ++this.quoteVersion;
    const date = this.formatDate(value.date);
    this.formError.set(null); this.purchaseQuote.set(null); this.redemptionQuote.set(null);
    if (this.mode() === 'purchase') {
      if (!value.issueId) { this.formError.set('Wybierz emisję TOS.'); return; }
      this.quoting.set(true);
      this.api.purchaseQuote(this.accountId(), value.issueId, value.quantity, date)
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.quoting.set(false))).subscribe({
          next: quote => { if (version === this.quoteVersion) this.purchaseQuote.set(quote); },
          error: (error: HttpErrorResponse) => { if (version === this.quoteVersion) this.formError.set(error.error?.message ?? 'Nie udało się wycenić zakupu.'); }
        });
    } else {
      const position = this.selectedPosition();
      if (!position) return;
      this.quoting.set(true);
      this.api.redemptionQuote(this.accountId(), position.id, value.quantity, date)
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.quoting.set(false))).subscribe({
          next: quote => { if (version === this.quoteVersion) this.redemptionQuote.set(quote); },
          error: (error: HttpErrorResponse) => { if (version === this.quoteVersion) this.formError.set(error.error?.message ?? 'Nie udało się wycenić wykupu.'); }
        });
    }
  }
  protected confirm(): void {
    if (this.store.saving() || this.quoting()) return;
    const purchase = this.purchaseQuote(), redemption = this.redemptionQuote();
    if (this.mode() === 'purchase' && purchase?.sufficientFunds) {
      this.store.purchaseBonds(this.accountId(), { requestId: this.requestId, issueId: purchase.issueId,
        quantity: purchase.quantity, date: purchase.date }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => { this.mode.set(null); this.saved.emit('Zakup TOS został zapisany. Gotówka została zamieniona na obligacje.'); },
          error: (error: HttpErrorResponse) => this.formError.set(error.error?.message ?? 'Nie udało się zapisać zakupu. Możesz ponowić operację.')
        });
    } else if (this.mode() === 'redemption' && redemption) {
      this.store.redeemBonds(this.accountId(), redemption.bondId, { requestId: this.requestId,
        quantity: redemption.quantity, date: redemption.instructionDate }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => { this.mode.set(null); this.saved.emit('Dyspozycja wykupu została zapisana. Gotówka wróci na to konto w dniu rozliczenia.'); },
          error: (error: HttpErrorResponse) => this.formError.set(error.error?.message ?? 'Nie udało się zapisać dyspozycji. Możesz ponowić operację.')
        });
    }
  }
  private formatDate(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
