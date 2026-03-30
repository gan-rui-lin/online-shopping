const TOKEN_KEY = 'token'
const TOKEN_HEAD_KEY = 'token_head'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getTokenHead(): string {
  return localStorage.getItem(TOKEN_HEAD_KEY) || 'Bearer '
}

export function setTokenHead(head: string): void {
  localStorage.setItem(TOKEN_HEAD_KEY, head)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_HEAD_KEY)
}

export function getItem<T>(key: string): T | null {
  const raw = localStorage.getItem(key)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setItem(key: string, value: unknown): void {
  localStorage.setItem(key, JSON.stringify(value))
}

export function removeItem(key: string): void {
  localStorage.removeItem(key)
}
