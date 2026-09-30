import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { ArrowRightLeft, CreditCard, LayoutDashboard, LogOut, Menu, ReceiptText, ShieldCheck, UserRound, UsersRound, WalletCards, X } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

const customerLinks = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/accounts', label: 'My accounts', icon: CreditCard },
  { to: '/transfer', label: 'Transfer money', icon: ArrowRightLeft },
  { to: '/transactions', label: 'Transactions', icon: ReceiptText },
  { to: '/beneficiaries', label: 'Beneficiaries', icon: UsersRound },
  { to: '/profile', label: 'Profile', icon: UserRound },
]

const adminLinks = [
  { to: '/admin', label: 'Admin dashboard', icon: ShieldCheck },
  { to: '/admin/users', label: 'Users', icon: UsersRound },
  { to: '/admin/accounts', label: 'Accounts', icon: WalletCards },
  { to: '/admin/transactions', label: 'All transactions', icon: ReceiptText },
]

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const links = user?.role === 'ADMIN' ? [...customerLinks, ...adminLinks] : customerLinks

  function signOut() {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <button className="mobile-menu" onClick={() => setOpen(!open)} aria-label="Toggle navigation">
        {open ? <X size={21} /> : <Menu size={21} />}
      </button>
      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <div className="brand"><span className="brand-mark">IB</span><span>Internet Banking System</span></div>
        <div className="profile-chip"><div className="avatar">{user?.firstName[0]}{user?.lastName[0]}</div><div><strong>{user?.firstName} {user?.lastName}</strong><small>{user?.role === 'ADMIN' ? 'Administrator' : 'Customer'}</small></div></div>
        <nav>{links.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} end={to === '/'} onClick={() => setOpen(false)}><Icon size={18} />{label}</NavLink>)}</nav>
        <button className="logout-button" onClick={signOut}><LogOut size={18} />Sign out</button>
      </aside>
      <main className="main-content"><Outlet /></main>
    </div>
  )
}

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: React.ReactNode }) {
  return <header className="page-header"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1>{description && <p className="page-description">{description}</p>}</div>{action}</header>
}

export function Loading({ label = 'Loading' }: { label?: string }) { return <div className="loading"><span className="spinner" />{label}...</div> }
export function EmptyState({ title, text }: { title: string; text: string }) { return <div className="empty-state"><div className="empty-icon"><WalletCards size={22} /></div><strong>{title}</strong><p>{text}</p></div> }
export function ErrorMessage({ message }: { message: string }) { return <div className="error-message" role="alert">{message}</div> }

export function AccountCard({ account, onSelect }: { account: import('../types/banking').Account; onSelect?: () => void }) {
  return <article className="account-card"><div className="account-card-top"><span className="account-type">{account.accountType}</span><span className={`status status-${account.status.toLowerCase()}`}>{account.status}</span></div><p className="account-number">{account.accountNumber}</p><p className="balance-label">Available balance</p><strong className="account-balance">{formatCurrency(account.balance)}</strong>{onSelect && <button className="text-button" onClick={onSelect}>View account <span>→</span></button>}</article>
}

export function TransactionTable({ transactions, compact = false }: { transactions: import('../types/banking').Transaction[]; compact?: boolean }) {
  return <div className="table-wrap"><table><thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>From / To</th><th>Description</th></tr></thead><tbody>{transactions.slice(0, compact ? 5 : undefined).map((transaction) => <tr key={transaction.transactionId}><td>{formatDate(transaction.transactionDate)}</td><td><span className={`transaction-dot dot-${transaction.transactionType.toLowerCase()}`} />{transaction.transactionType}</td><td className={`amount-${transaction.transactionType.toLowerCase()}`}>{transaction.transactionType === 'WITHDRAW' || transaction.fromAccountNumber ? '-' : '+'}{formatCurrency(transaction.amount)}</td><td>{transaction.fromAccountNumber || 'External'} <span className="muted-arrow">→</span> {transaction.toAccountNumber || 'External'}</td><td>{transaction.description || 'Account activity'}</td></tr>)}</tbody></table>{transactions.length === 0 && <EmptyState title="No transactions yet" text="Your account activity will appear here." />}</div>
}

export function formatCurrency(value: number) { return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value) }
export function formatDate(value: string) { return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value)) }