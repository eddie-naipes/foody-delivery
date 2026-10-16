import { Navigate, Outlet, useLocation } from 'react-router'

import { useAuth } from './useAuth'

/** Só deixa passar quem tem sessão; guarda a página pedida para voltar a ela após o login. */
export function ProtectedRoute() {
  const { session } = useAuth()
  const location = useLocation()
  return session ? <Outlet /> : <Navigate to="/login" replace state={{ from: location.pathname }} />
}

/** Login e cadastro não fazem sentido para quem já entrou. */
export function PublicOnlyRoute() {
  const { session } = useAuth()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from
  return session ? <Navigate to={from ?? '/pedidos'} replace /> : <Outlet />
}
