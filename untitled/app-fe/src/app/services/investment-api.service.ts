import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AccountDeletedResponse, DeleteAccountRequest } from '../models/account.model';
import { DepositRequest, InvestmentAccountRequest, InvestmentDetailsDto, InvestmentMutationResponse } from '../models/investment.model';

@Injectable({ providedIn: 'root' })
export class InvestmentApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/finance/investments';

  createAccount(request: InvestmentAccountRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.post<InvestmentMutationResponse>(this.url, request, { params: month ? { month } : {} });
  }

  configureDeposit(id: number, request: DepositRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.put<InvestmentMutationResponse>(`${this.url}/${id}/deposit`, request, { params: month ? { month } : {} });
  }

  createDeposit(request: DepositRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.post<InvestmentMutationResponse>(`${this.url}/deposits`, request, { params: month ? { month } : {} });
  }

  getDetails(id: number): Observable<InvestmentDetailsDto> {
    return this.http.get<InvestmentDetailsDto>(`${this.url}/${id}`);
  }

  deleteAccount(id: number, request: DeleteAccountRequest, month?: string): Observable<AccountDeletedResponse> {
    return this.http.delete<AccountDeletedResponse>(`${this.url}/${id}`, { body: request, params: month ? { month } : {} });
  }
}
