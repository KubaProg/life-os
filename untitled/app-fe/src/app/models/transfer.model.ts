import type { FinanceOverviewDto } from './finance-overview.model';

export interface AccountReference {
  kind: 'ORDINARY' | 'INVESTMENT';
  id: number;
}
export interface TransferRequest {
  requestId: string;
  sourceAccountId: number;
  destinationAccountId: number;
  amount: number;
  date: string;
}
export interface TransferDto {
  id: number;
  source: AccountReference;
  sourceName: string;
  destination: AccountReference;
  destinationName: string;
  amount: number;
  currency: string;
  date: string;
}
export interface TransferResponse {
  transferId: number;
  overview: FinanceOverviewDto;
}
