import client from './client'
import type { Account, AccountRequest, AmountRequest, Beneficiary, BeneficiaryRequest, Transaction, TransferRequest, User } from '../types/banking'

export const accountApi = {
  list: () => client.get<Account[]>('/api/accounts/my'),
  get: (number: string) => client.get<Account>(`/api/accounts/${encodeURIComponent(number)}`),
  create: (request: AccountRequest) => client.post<Account>('/api/accounts', request),
  deposit: (number: string, request: AmountRequest) => client.post<Account>(`/api/accounts/${encodeURIComponent(number)}/deposit`, request),
  withdraw: (number: string, request: AmountRequest) => client.post<Account>(`/api/accounts/${encodeURIComponent(number)}/withdraw`, request),
}

export const transferApi = {
  create: (request: TransferRequest) => client.post<void>('/api/transfers', request),
}

export const transactionApi = {
  list: (number: string) => client.get<Transaction[]>(`/api/transactions/my/${encodeURIComponent(number)}`),
}

export const beneficiaryApi = {
  list: () => client.get<Beneficiary[]>('/api/beneficiaries/my'),
  create: (request: BeneficiaryRequest) => client.post<Beneficiary>('/api/beneficiaries', request),
  remove: (id: number) => client.delete(`/api/beneficiaries/${id}`),
}

export const adminApi = {
  users: () => client.get<User[]>('/api/admin/users'),
  accounts: () => client.get<Account[]>('/api/admin/accounts'),
  transactions: () => client.get<Transaction[]>('/api/admin/transactions'),
  freeze: (number: string) => client.patch<Account>(`/api/admin/accounts/${encodeURIComponent(number)}/freeze`),
  unfreeze: (number: string) => client.patch<Account>(`/api/admin/accounts/${encodeURIComponent(number)}/unfreeze`),
}