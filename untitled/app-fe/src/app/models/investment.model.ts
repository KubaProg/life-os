import type { FinanceOverviewDto } from './finance-overview.model';

export type InvestmentType = 'DEPOSIT' | 'BOND' | 'ETF' | 'OTHER';
export interface InvestmentDto {
  id: number;
  name: string;
  type: InvestmentType;
  currency: string;
  principal: number;
  currentValue: number;
  deletionRefund: number;
  grossProfit: number;
  estimatedTax: number;
  netProfit: number;
  annualRate: number | null;
  taxRate: number | null;
  calculationRule: string | null;
  capitalization: 'AT_MATURITY' | null;
  startDate: string | null;
  maturityDate: string | null;
  valuedOn: string;
  status: 'DRAFT' | 'READY' | 'AWAITING_FUNDING' | 'ACTIVE' | 'MATURED' | 'SETTLED';
}
export interface InvestmentAccountRequest {
  name: string;
  currency: string;
  type: InvestmentType;
}
export interface DepositRequest {
  requestId: string;
  name: string;
  sourceAccountId: number;
  principal: number;
  annualRate: number;
  taxRate: number;
  startDate: string;
  maturityDate: string;
}
export interface InvestmentOperationDto {
  id: number;
  transferId: number;
  type: 'FUNDING' | 'PRINCIPAL_RETURN' | 'INTEREST_PAYOUT' | 'DEPOSIT_CANCELLATION';
  date: string;
  currency: string;
  amount: number;
}
export interface InvestmentValuationDto {
  date: string;
  currentValue: number;
  grossProfit: number;
  estimatedTax: number;
  netProfit: number;
  source: string;
}
export interface InvestmentDetailsDto {
  investment: InvestmentDto;
  valuations: ReadonlyArray<InvestmentValuationDto>;
  operations: ReadonlyArray<InvestmentOperationDto>;
}
export interface InvestmentMutationResponse {
  investmentAccountId: number;
  overview: FinanceOverviewDto;
}
export const investmentTypeLabel = (type: InvestmentType): string => ({
  DEPOSIT: 'Lokata', BOND: 'Obligacje', ETF: 'ETF', OTHER: 'Inwestycja'
})[type];
export const investmentStatusLabel = (status: InvestmentDto['status']): string => ({
  DRAFT: 'Do konfiguracji', READY: 'Gotowe', AWAITING_FUNDING: 'Oczekuje na wpłatę', ACTIVE: 'Aktywna', MATURED: 'Do wypłaty', SETTLED: 'Rozliczona'
})[status];
export const investmentRoute = (type: InvestmentType): string => ({
  DEPOSIT: '/finanse/lokaty', BOND: '/finanse/obligacje', ETF: '/finanse/rachunki-maklerskie', OTHER: '/finanse/rachunki-maklerskie'
})[type];
export const investmentAccountKind = (type: InvestmentType): 'DEPOSIT' | 'BOND' | 'BROKERAGE' =>
  type === 'DEPOSIT' ? 'DEPOSIT' : type === 'BOND' ? 'BOND' : 'BROKERAGE';
export const investmentOperationLabel = (type: InvestmentOperationDto['type']): string => ({
  FUNDING: 'Założenie lokaty', PRINCIPAL_RETURN: 'Zwrot kapitału', INTEREST_PAYOUT: 'Wypłata odsetek',
  DEPOSIT_CANCELLATION: 'Zerwanie lokaty — zwrot kapitału bez odsetek'
})[type];
