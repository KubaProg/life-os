import { AccountDto } from './account.model';
import { TransactionDto } from './transaction.model';

export interface MoneyTotalDto {
  currency: string;
  amount: number;
}

export interface FinanceOverviewDto {
  accountTotals: ReadonlyArray<MoneyTotalDto>;
  accounts: ReadonlyArray<AccountDto>;
  transactions: ReadonlyArray<TransactionDto>;
}

export interface TransactionCreatedResponse {
  createdTransaction: TransactionDto;
  overview: FinanceOverviewDto;
}
