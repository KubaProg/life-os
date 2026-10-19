import type { InvestmentDto } from './investment.model';
import type { TransferDto } from './transfer.model';
import { AccountDto } from './account.model';
import { TransactionDto } from './transaction.model';

export interface MoneyTotalDto {
  currency: string;
  amount: number;
}

export interface InvestmentProfitDto {
  month: string;
  mocked: boolean;
  totals: ReadonlyArray<MoneyTotalDto>;
}

export interface MonthlySummaryDto {
  month: string;
  totals: ReadonlyArray<{
    currency: string;
    income: number;
    expenses: number;
    cashflow: number;
    savingsRate: number | null;
  }>;
}

export interface FinanceOverviewDto {
  investmentProfit: InvestmentProfitDto;
  monthlySummary: MonthlySummaryDto;
  accountTotals: ReadonlyArray<MoneyTotalDto>;
  accounts: ReadonlyArray<AccountDto>;
  investments: ReadonlyArray<InvestmentDto>;
  investmentTotals: ReadonlyArray<MoneyTotalDto>;
  transfers: ReadonlyArray<TransferDto>;
  transactions: ReadonlyArray<TransactionDto>;
}

export interface TransactionCreatedResponse {
  createdTransaction: TransactionDto;
  overview: FinanceOverviewDto;
}

export interface TransactionUpdatedResponse {
  updatedTransaction: TransactionDto;
  overview: FinanceOverviewDto;
}

export interface TransactionDeletedResponse {
  deletedTransactionId: number;
  overview: FinanceOverviewDto;
}
