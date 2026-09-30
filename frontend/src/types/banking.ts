export type User = {
  userId: number
  firstName: string
  lastName: string
  email: string
  role: 'CUSTOMER' | 'ADMIN'
}

export type Account = {
  accountId: number
  accountNumber: string
  accountType: string
  balance: number
  status: 'ACTIVE' | 'FROZEN'
}

export type Transaction = {
  transactionId: number
  fromAccountNumber: string | null
  toAccountNumber: string | null
  amount: number
  transactionType: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER'
  transactionDate: string
  description: string | null
}

export type Beneficiary = {
  beneficiaryId: number
  name: string
  accountNumber: string
  bankName: string
  createdAt: string
}

export type RegisterRequest = {
  firstName: string
  lastName: string
  email: string
  password: string
}

export type AccountRequest = { accountNumber: string; accountType: string }
export type AmountRequest = { amount: number }
export type TransferRequest = {
  senderAccountNumber: string
  receiverAccountNumber: string
  amount: number
}
export type BeneficiaryRequest = {
  name: string
  accountNumber: string
  bankName: string
}