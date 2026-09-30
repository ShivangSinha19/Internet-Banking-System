import client from './client'
import type { RegisterRequest, User } from '../types/banking'

export const authApi = {
  register: (request: RegisterRequest) => client.post<User>('/api/auth/register', request),
  login: (email: string, password: string) => client.post<User>('/api/auth/login', { email, password }),
  me: () => client.get<User>('/api/auth/me'),
}