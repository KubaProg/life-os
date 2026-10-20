import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AccountReference, TransferDto } from '../../../../models/transfer.model';

@Component({
  selector: 'app-transfer-history',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  template: `
    <section class="history" [attr.aria-labelledby]="idPrefix() + '-title'">
      <h2 [id]="idPrefix() + '-title'">Transfery</h2>
      @for (transfer of transfers(); track transfer.id) {
        <article>
          <div><a [routerLink]="route(transfer.source)">{{ transfer.sourceName }}</a>
            <span aria-label="do"> → </span><a [routerLink]="route(transfer.destination)">{{ transfer.destinationName }}</a>
            <p>{{ transfer.date | date: 'd MMM y' : undefined : 'pl-PL' }}</p></div>
          <strong>{{ transfer.amount | currency: transfer.currency : 'symbol' : '1.2-2' : 'pl-PL' }}</strong>
        </article>
      } @empty { <p>Brak transferów.</p> }
    </section>
  `,
  styles: `
    :host { display: block; }
    .history { margin-top: 1rem; padding: 1.25rem; background: #fff; border: 1px solid #dce2dc; border-radius: 1.35rem; }
    h2 { margin: 0 0 1rem; font-size: 1.35rem; }
    article { display: grid; gap: .5rem; padding: .75rem 0; border-top: 1px solid #e8ede7; overflow-wrap: anywhere; }
    a { display: inline-flex; align-items: center; min-height: 44px; color: #285a3d; }
    p { margin: .25rem 0 0; color: #68766c; font-size: .8rem; }
    strong { font-size: .9rem; }
    @media (min-width: 480px) { article { grid-template-columns: minmax(0, 1fr) auto; align-items: center; } }
  `
})
export class TransferHistory {
  readonly transfers = input<ReadonlyArray<TransferDto>>([]);
  readonly idPrefix = input.required<string>();
  protected route(reference: AccountReference): Array<string | number> {
    const paths = { ORDINARY: '/finanse/konta', DEPOSIT: '/finanse/lokaty', BOND: '/finanse/obligacje', BROKERAGE: '/finanse/rachunki-maklerskie' };
    return [paths[reference.kind], reference.id];
  }
}
