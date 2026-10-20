import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  AccountDetailsDto,
  AccountDeletedResponse,
  DeleteAccountRequest,
  AccountDto,
  AccountRequest,
  OpeningBalanceRequest,
  UpdateAccountRequest
} from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountApiService {
  private readonly http = inject(HttpClient);
  private readonly accountsUrl = '/api/finance/accounts';

  getAccounts(): Observable<AccountDto[]> {
    return this.http.get<AccountDto[]>(this.accountsUrl);
  }

  getAccount(accountId: number): Observable<AccountDetailsDto> {
    return this.http.get<AccountDetailsDto>(`${this.accountsUrl}/${accountId}`);
  }

  createAccount(request: AccountRequest): Observable<AccountDto> {
    return this.http.post<AccountDto>(this.accountsUrl, request);
  }

  deleteAccount(id: number, request: DeleteAccountRequest, month?: string): Observable<AccountDeletedResponse> {
    return this.http.delete<AccountDeletedResponse>(`${this.accountsUrl}/${id}`, { body: request, params: month ? { month } : {} });
  }

  configureOpeningBalance(id: number, request: OpeningBalanceRequest): Observable<AccountDto> {
    return this.http.put<AccountDto>(`${this.accountsUrl}/${id}/opening-balance`, request);
  }

  updateAccount(accountId: number, request: UpdateAccountRequest): Observable<AccountDto> {
    return this.http.patch<AccountDto>(`${this.accountsUrl}/${accountId}`, request);
  }
}
