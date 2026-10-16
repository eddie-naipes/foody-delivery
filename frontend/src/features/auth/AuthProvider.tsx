import { useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'

import { onSessionRejected, setAuthToken } from '@/shared/api/http'
import { MESSAGES } from '@/shared/messages'

import { authApi } from './api'
import type { LoginValues, RegisterValues } from './schemas'
import { clearSession, loadSession, saveSession, type Session } from './session'

export interface AuthContextValue {
  session: Session | null
  /** Aviso para a tela de login, como "sua sessão expirou". */
  notice: string | null
  login: (values: LoginValues) => Promise<void>
  register: (values: RegisterValues) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

function restoreSession() {
  const session = loadSession()
  // O token precisa estar no cliente HTTP antes da primeira requisição das telas filhas
  setAuthToken(session?.token ?? null)
  return session
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [session, setSession] = useState<Session | null>(restoreSession)
  const [notice, setNotice] = useState<string | null>(null)

  const start = useCallback((next: Session) => {
    setAuthToken(next.token)
    saveSession(next)
    setNotice(null)
    setSession(next)
  }, [])

  const end = useCallback(
    (reason: string | null) => {
      setAuthToken(null)
      clearSession()
      // Dados do usuário anterior não podem aparecer para o próximo
      queryClient.clear()
      setNotice(reason)
      setSession(null)
    },
    [queryClient],
  )

  useEffect(() => onSessionRejected(() => end(MESSAGES.sessionExpired)), [end])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      notice,
      login: async (values) => start(await authApi.login(values)),
      register: async (values) => start(await authApi.register(values)),
      logout: () => end(null),
    }),
    [session, notice, start, end],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
