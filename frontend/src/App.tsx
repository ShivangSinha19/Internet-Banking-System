import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { useAuth } from './context/AuthContext'
import { AccountDetailsPage, AccountsPage, AdminPage, BeneficiariesPage, CreateAccountPage, DashboardPage, LoginPage, MoneyActionPage, ProfilePage, RegisterPage, TransactionsPage, TransferPage } from './pages'

function Protected({ children, admin = false }: { children: React.ReactNode; admin?: boolean }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (admin && user.role !== 'ADMIN') return <Navigate to="/" replace />
  return <>{children}</>
}

export default function App() {
  return <Routes><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /><Route element={<Protected><Layout /></Protected>}><Route index element={<DashboardPage />} /><Route path="accounts" element={<AccountsPage />} /><Route path="accounts/new" element={<CreateAccountPage />} /><Route path="accounts/:accountNumber" element={<AccountDetailsPage />} /><Route path="accounts/:accountNumber/deposit" element={<MoneyActionPage kind="deposit" />} /><Route path="accounts/:accountNumber/withdraw" element={<MoneyActionPage kind="withdraw" />} /><Route path="transfer" element={<TransferPage />} /><Route path="transactions" element={<TransactionsPage />} /><Route path="beneficiaries" element={<BeneficiariesPage />} /><Route path="profile" element={<ProfilePage />} /><Route path="admin" element={<Protected admin><AdminPage /></Protected>} /><Route path="admin/users" element={<Protected admin><AdminPage /></Protected>} /><Route path="admin/accounts" element={<Protected admin><AdminPage /></Protected>} /><Route path="admin/transactions" element={<Protected admin><AdminPage /></Protected>} /></Route><Route path="*" element={<Navigate to="/" replace />} /></Routes>
}