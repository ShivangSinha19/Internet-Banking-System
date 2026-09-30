import { createContext, useContext, useState, type ReactNode } from 'react'
import { authApi } from '../api/authApi'
import { clearBasicAuth, setBasicAuth } from '../api/client'
import type { User } from '../types/banking'

type AuthContextValue = {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (request: { firstName: string; lastName: string; email: string; password: string }) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(false)

  async function login(email: string, password: string) {
    setLoading(true)
    try {
      const response = await authApi.login(email, password)
      setBasicAuth(email, password)
      setUser(response.data)
    } finally {
      setLoading(false)
    }
  }

  async function register(request: { firstName: string; lastName: string; email: string; password: string }) {
    setLoading(true)
    try {
      await authApi.register(request)
      await login(request.email, request.password)
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    clearBasicAuth()
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, loading, login, register, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used inside AuthProvider')
  return value
}