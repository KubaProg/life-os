export type FinancialAccountType =
  | 'BANK_ACCOUNT'
  | 'BROKERAGE'
  | 'CASH'
  | 'SAVINGS'
  | 'OTHER';

export type OrdinaryAccountType = Extract<
  FinancialAccountType,
  'BANK_ACCOUNT' | 'SAVINGS' | 'CASH'
>;

export interface AccountDto {
  id: number;
  name: string;
  type: FinancialAccountType;
  currency: string;
  currentBalance: number;
  balanceStartDate: string;
}

export interface AccountRequest {
  name: string;
  type: OrdinaryAccountType;
  currency: string;
  openingBalance?: number;
  balanceStartDate?: string;
}

export interface UpdateAccountRequest {
  name?: string;
  type?: OrdinaryAccountType;
  currency?: string;
}

export interface OpeningBalanceRequest {
  openingBalance: number;
  balanceStartDate: string;
}

export interface AccountDeletedResponse {
  deletedAccountId: number;
  overview: import('./finance-overview.model').FinanceOverviewDto;
}

export interface DeleteAccountRequest {
  destinationAccountId?: number;
  expectedBalance: number;
}

export interface AccountDetailsDto {
  account: AccountDto;
  transactions: ReadonlyArray<import('./transaction.model').TransactionDto>;
  transfers: ReadonlyArray<import('./transfer.model').TransferDto>;
}
