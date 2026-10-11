export type TransactionType = 'INCOME' | 'EXPENSE';

export interface TransactionDto {
  id: number;
  accountId: number;
  accountName: string;
  type: TransactionType;
  amount: number;
  currency: string;
  transactionDate: string;
  category: string;
  description: string | null;
}

export interface TransactionRequest {
  accountId: number;
  type: TransactionType;
  amount: number;
  transactionDate: string;
  category: string;
  description?: string;
}
