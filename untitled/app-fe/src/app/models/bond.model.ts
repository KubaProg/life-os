export interface BondIssueDto {
  id: number; code: string; type: 'TOS'; currency: string; unitPrice: number;
  annualRate: number; taxRate: number; durationYears: number; earlyRedemptionFee: number;
  saleFrom: string; saleUntil: string; sourceUrl: string;
}
export interface BondPositionDto {
  id: number; issueCode: string; purchaseDate: string; maturityDate: string;
  originalQuantity: number; remainingQuantity: number; pendingQuantity: number; availableQuantity: number;
  principal: number; grossValue: number; currentValue: number; grossProfit: number;
  estimatedTax: number; netProfit: number; annualRate: number; realizedProfit: number;
  earliestRedemptionDate: string; latestRedemptionDate: string; canRedeem: boolean;
  status: 'ACTIVE' | 'REDEMPTION_PENDING' | 'CLOSED';
}
export interface BondOperationDto {
  id: number; bondId: number; issueCode: string;
  type: 'PURCHASE' | 'CAPITALIZATION' | 'EARLY_REDEMPTION' | 'MATURITY_REDEMPTION';
  status: 'PENDING' | 'SETTLED'; instructionDate: string; settlementDate: string;
  quantity: number; principal: number; grossInterest: number; fee: number; tax: number; netCash: number;
}
export interface BondDetailsDto {
  accountId: number; availableCash: number; principal: number; grossValue: number;
  currentValue: number; grossProfit: number; estimatedTax: number; netProfit: number; realizedProfit: number;
  positions: ReadonlyArray<BondPositionDto>; operations: ReadonlyArray<BondOperationDto>;
}
export interface BondPurchaseRequest { requestId: string; issueId: number; quantity: number; date: string; }
export interface BondRedemptionRequest { requestId: string; quantity: number; date: string; }
export interface BondPurchaseQuote {
  issueId: number; quantity: number; date: string; maturityDate: string;
  cost: number; availableCash: number; sufficientFunds: boolean;
}
export interface BondRedemptionQuote {
  bondId: number; quantity: number; instructionDate: string; settlementDate: string;
  principal: number; grossInterest: number; fee: number; tax: number; netCash: number;
}
export const bondOperationLabel = (type: BondOperationDto['type']): string => ({
  PURCHASE: 'Zakup', CAPITALIZATION: 'Kapitalizacja roczna', EARLY_REDEMPTION: 'Przedterminowy wykup',
  MATURITY_REDEMPTION: 'Wykup w terminie'
})[type];
