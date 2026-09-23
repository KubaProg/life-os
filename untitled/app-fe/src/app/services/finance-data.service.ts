import { Injectable } from '@angular/core';

export type AccountType = 'Osobiste' | 'Oszczędnościowe' | 'Gotówka' | 'Inwestycyjne';

export interface FinanceAccount {
  id: string;
  name: string;
  institution: string;
  type: AccountType;
  balance: number;
  color: string;
  accountNumber: string;
}

export interface FinanceTransaction {
  id: string;
  accountId: string;
  name: string;
  category: string;
  date: Date;
  amount: number;
  icon: string;
}

@Injectable({ providedIn: 'root' })
export class FinanceDataService {
  readonly accounts: FinanceAccount[] = [
    { id: 'konto-glowne', name: 'Konto główne', institution: 'mBank', type: 'Osobiste', balance: 8420.55, color: '#285a3d', accountNumber: 'PL 11 1140 2004 0000 3002 0135 4321' },
    { id: 'oszczednosci', name: 'Oszczędności', institution: 'mBank', type: 'Oszczędnościowe', balance: 14500, color: '#6c7a5a', accountNumber: 'PL 11 1140 2004 0000 3002 0135 9876' },
    { id: 'portfel', name: 'Portfel', institution: 'Gotówka', type: 'Gotówka', balance: 320, color: '#bd8a4d', accountNumber: 'Środki fizyczne' },
    { id: 'inwestycje', name: 'Inwestycje', institution: 'Bossa', type: 'Inwestycyjne', balance: 12360.4, color: '#536d8c', accountNumber: 'Rachunek maklerski' }
  ];

  readonly transactions: FinanceTransaction[] = [
    { id: 'biedronka-22-09', accountId: 'konto-glowne', name: 'Biedronka', category: 'Zakupy', date: new Date(2026, 8, 22), amount: -126.48, icon: 'B' },
    { id: 'wynagrodzenie-20-09', accountId: 'konto-glowne', name: 'Wynagrodzenie', category: 'Przychody', date: new Date(2026, 8, 20), amount: 9200, icon: 'W' },
    { id: 'spotify-19-09', accountId: 'konto-glowne', name: 'Spotify', category: 'Rozrywka', date: new Date(2026, 8, 19), amount: -23.99, icon: 'S' },
    { id: 'czynsz-15-09', accountId: 'konto-glowne', name: 'Czynsz', category: 'Dom', date: new Date(2026, 8, 15), amount: -1850, icon: 'C' },
    { id: 'przelew-na-oszczednosci-18-09', accountId: 'oszczednosci', name: 'Przelew na oszczędności', category: 'Oszczędzanie', date: new Date(2026, 8, 18), amount: 1500, icon: 'O' },
    { id: 'gotowka-16-09', accountId: 'portfel', name: 'Wypłata gotówki', category: 'Zasilenie', date: new Date(2026, 8, 16), amount: 500, icon: 'G' },
    { id: 'etf-14-09', accountId: 'inwestycje', name: 'Zakup ETF MSCI World', category: 'Inwestycje', date: new Date(2026, 8, 14), amount: 860, icon: 'E' }
  ];

  getAccount(id: string | null): FinanceAccount | undefined {
    return this.accounts.find((account) => account.id === id);
  }

  getTransactionsForAccount(accountId: string): FinanceTransaction[] {
    return this.transactions.filter((transaction) => transaction.accountId === accountId);
  }
}
