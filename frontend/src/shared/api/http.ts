import axios, { isAxiosError } from 'axios'

import { toApiError } from './errors'

/**
 * Cliente HTTP único da aplicação. Fala sempre com "/api" na mesma origem (proxy do
 * Vite em desenvolvimento, Nginx em produção), injeta o token e entrega erros já
 * normalizados como ApiError.
 *
 * Não conhece a feature de autenticação: ela informa o token com setAuthToken e
 * escuta a recusa de sessão com onSessionRejected.
 */
export const http = axios.create({ baseURL: '/api' })

let authToken: string | null = null
const sessionRejectedListeners = new Set<() => void>()

export function setAuthToken(token: string | null) {
  authToken = token
}

/** Avisado quando a API recusa o token atual (expirado ou inválido). Devolve a função para cancelar. */
export function onSessionRejected(listener: () => void) {
  sessionRejectedListeners.add(listener)
  return () => {
    sessionRejectedListeners.delete(listener)
  }
}

http.interceptors.request.use((config) => {
  if (authToken) config.headers.Authorization = `Bearer ${authToken}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const apiError = toApiError(error)
    // 401 em rota protegida = sessão inválida (token expirado, adulterado ou ausente).
    // Em /auth/* o 401 é só credencial recusada no login e não deve encerrar nada.
    const url = isAxiosError(error) ? (error.config?.url ?? '') : ''
    if (apiError.status === 401 && !url.startsWith('/auth/')) {
      sessionRejectedListeners.forEach((listener) => listener())
    }
    return Promise.reject(apiError)
  },
)
