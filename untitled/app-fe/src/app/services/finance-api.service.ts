import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  FinanceOverviewDto,
  TransactionCreatedResponse,
  TransactionUpdatedResponse,
  TransactionDeletedResponse
} from '../models/finance-overview.model';
import { TransferRequest, TransferResponse } from '../models/transfer.model';
import { TransactionRequest } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class FinanceApiService {
  private readonly http = inject(HttpClient);
  private readonly overviewUrl = '/api/finance/overview';
  private readonly transactionsUrl = '/api/finance/transactions';

  getOverview(month?: string): Observable<FinanceOverviewDto> {
    return this.http.get<FinanceOverviewDto>(this.overviewUrl, { params: month ? { month } : {} });
  }

  transfer(request: TransferRequest, month?: string): Observable<TransferResponse> {
    return this.http.post<TransferResponse>('/api/finance/transfers', request, { params: month ? { month } : {} });
  }

  updateTransaction(id: number, request: TransactionRequest, month?: string): Observable<TransactionUpdatedResponse> {
    return this.http.put<TransactionUpdatedResponse>(`${this.transactionsUrl}/${id}`, request, { params: month ? { month } : {} });
  }

  createTransaction(request: TransactionRequest, month?: string): Observable<TransactionCreatedResponse> {
    return this.http.post<TransactionCreatedResponse>(this.transactionsUrl, request, { params: month ? { month } : {} });
  }

  deleteTransaction(id: number, month?: string): Observable<TransactionDeletedResponse> {
    return this.http.delete<TransactionDeletedResponse>(`${this.transactionsUrl}/${id}`, { params: month ? { month } : {} });
  }
}
