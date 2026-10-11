import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  FinanceOverviewDto,
  TransactionCreatedResponse
} from '../models/finance-overview.model';
import { TransactionRequest } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class FinanceApiService {
  private readonly http = inject(HttpClient);
  private readonly overviewUrl = '/api/finance/overview';
  private readonly transactionsUrl = '/api/finance/transactions';

  getOverview(): Observable<FinanceOverviewDto> {
    return this.http.get<FinanceOverviewDto>(this.overviewUrl);
  }

  createTransaction(request: TransactionRequest): Observable<TransactionCreatedResponse> {
    return this.http.post<TransactionCreatedResponse>(this.transactionsUrl, request);
  }
}
