import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DepositRequest, InvestmentDetailsDto, InvestmentMutationResponse } from '../models/investment.model';

@Injectable({ providedIn: 'root' })
export class InvestmentApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/finance/investments';

  createDeposit(request: DepositRequest, month?: string): Observable<InvestmentMutationResponse> {
    return this.http.post<InvestmentMutationResponse>(`${this.url}/deposits`, request, { params: month ? { month } : {} });
  }

  getDetails(id: number): Observable<InvestmentDetailsDto> {
    return this.http.get<InvestmentDetailsDto>(`${this.url}/${id}`);
  }
}
