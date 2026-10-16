import { use } from 'react'

import { AuthContext, type AuthContextValue } from './AuthProvider'

export function useAuth(): AuthContextValue {
  const context = use(AuthContext)
  if (!context) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return context
}

/** Para telas que só existem com sessão ativa (dentro de <ProtectedRoute>). */
export function useSessionUser() {
  const { session } = useAuth()
  if (!session) throw new Error('useSessionUser exige uma sessão ativa')
  return session.user
}
