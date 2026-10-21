import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BondDetailsDto, BondIssueDto, BondPurchaseQuote, BondPurchaseRequest, BondRedemptionQuote, BondRedemptionRequest } from '../models/bond.model';
import { InvestmentMutationResponse } from '../models/investment.model';

@Injectable({ providedIn: 'root' })
export class BondApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/finance/bonds';
  issues(): Observable<BondIssueDto[]> { return this.http.get<BondIssueDto[]>(`${this.url}/issues`); }
  details(accountId: number): Observable<BondDetailsDto> { return this.http.get<BondDetailsDto>(`${this.url}/accounts/${accountId}`); }
  purchaseQuote(accountId: number, issueId: number, quantity: number, date: string): Observable<BondPurchaseQuote> {
    return this.http.get<BondPurchaseQuote>(`${this.url}/accounts/${accountId}/purchase-quote`, { params: { issueId, quantity, date } });
  }
  redemptionQuote(accountId: number, bondId: number, quantity: number, date: string): Observable<BondRedemptionQuote> {
    return this.http.get<BondRedemptionQuote>(`${this.url}/accounts/${accountId}/positions/${bondId}/redemption-quote`, { params: { quantity, date } });
  }
  purchase(accountId: number, request: BondPurchaseRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.post<InvestmentMutationResponse>(`${this.url}/accounts/${accountId}/purchases`, request, { params: month ? { month } : {} });
  }
  redeem(accountId: number, bondId: number, request: BondRedemptionRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.post<InvestmentMutationResponse>(`${this.url}/accounts/${accountId}/positions/${bondId}/redemptions`, request, { params: month ? { month } : {} });
  }
}
