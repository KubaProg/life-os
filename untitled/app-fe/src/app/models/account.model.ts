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
  openingBalance: number;
  balanceStartDate: string;
}

export interface UpdateAccountRequest {
  name?: string;
  type?: OrdinaryAccountType;
  currency?: string;
}
