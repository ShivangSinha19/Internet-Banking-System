import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

let basicAuth: string | null = null

export function setBasicAuth(email: string, password: string) {
  basicAuth = `Basic ${btoa(`${email}:${password}`)}`
}

export function clearBasicAuth() {
  basicAuth = null
}

client.interceptors.request.use((config) => {
  if (basicAuth) config.headers.Authorization = basicAuth
  return config
})

export function apiError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const serverMessage = error.response?.data?.message
    if (status === 401) return 'Invalid credentials or your session has expired.'
    if (status === 403) return 'You do not have permission for this action.'
    if (status === 404) return serverMessage || 'The requested resource was not found.'
    if (status === 409) return serverMessage || 'This record already exists.'
    if (status === 400) return serverMessage || 'Please check the submitted information.'
    return serverMessage || 'The banking service is temporarily unavailable.'
  }
  return 'Something went wrong. Please try again.'
}

export default client