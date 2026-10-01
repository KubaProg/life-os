import { FinancialAccountType } from '../../../models/account.model';

const ACCOUNT_TYPE_LABELS: Record<FinancialAccountType, string> = {
  BANK_ACCOUNT: 'Konto osobiste',
  BROKERAGE: 'Konto maklerskie',
  CASH: 'Gotówka',
  SAVINGS: 'Konto oszczędnościowe',
  OTHER: 'Inne'
};

const ACCOUNT_TYPE_COLORS: Record<FinancialAccountType, string> = {
  BANK_ACCOUNT: '#285a3d',
  BROKERAGE: '#536d8c',
  CASH: '#bd8a4d',
  SAVINGS: '#6c7a5a',
  OTHER: '#69736c'
};

export function accountTypeLabel(type: FinancialAccountType): string {
  return ACCOUNT_TYPE_LABELS[type];
}

export function accountTypeColor(type: FinancialAccountType): string {
  return ACCOUNT_TYPE_COLORS[type];
}
