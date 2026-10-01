import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AccountDto, AccountRequest, UpdateAccountRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountApiService {
  private readonly http = inject(HttpClient);
  private readonly accountsUrl = '/api/finance/accounts';

  getAccounts(): Observable<AccountDto[]> {
    return this.http.get<AccountDto[]>(this.accountsUrl);
  }

  getAccount(accountId: number): Observable<AccountDto> {
    return this.http.get<AccountDto>(`${this.accountsUrl}/${accountId}`);
  }

  createAccount(request: AccountRequest): Observable<AccountDto> {
    return this.http.post<AccountDto>(this.accountsUrl, request);
  }

  updateAccount(accountId: number, request: UpdateAccountRequest): Observable<AccountDto> {
    return this.http.patch<AccountDto>(`${this.accountsUrl}/${accountId}`, request);
  }
}
