import { useEffect } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router'

import { useAuth, useSessionUser } from '@/features/auth/useAuth'
import { cn } from '@/shared/lib/cn'
import { Brand } from '@/shared/ui/Brand'

const navLink = ({ isActive }: { isActive: boolean }) =>
  cn(
    'relative inline-flex h-full items-center px-1 text-[15px] font-bold transition-colors',
    "after:absolute after:inset-x-0 after:bottom-0 after:h-[3px] after:rounded-full after:content-['']",
    isActive ? 'text-brand after:bg-brand' : 'text-muted hover:text-ink after:bg-transparent',
  )

/** Moldura das telas logadas: marca, navegação com sublinhado vermelho, operador e saída. */
export function AppLayout() {
  const { logout } = useAuth()
  const user = useSessionUser()
  const { pathname } = useLocation()

  // Cada tela nova começa no topo, e não na altura em que a anterior estava rolada
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  return (
    <div className="min-h-dvh">
      <header className="border-b border-line bg-paper">
        <div className="mx-auto flex h-16 max-w-[1200px] items-center justify-between gap-4 px-4">
          <div className="flex h-full items-center gap-8">
            <Link to="/pedidos" aria-label="Foody Delivery, ir para pedidos">
              <Brand />
            </Link>
            <nav aria-label="Principal" className="hidden h-full items-center gap-6 sm:flex">
              <NavLink to="/pedidos" end className={navLink}>
                Pedidos
              </NavLink>
              <NavLink to="/pedidos/novo" className={navLink}>
                Novo pedido
              </NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-3 text-[15px]">
            <span className="hidden max-w-[30vw] truncate font-bold text-ink sm:inline">{user.name}</span>
            <button
              type="button"
              onClick={logout}
              className="h-9 rounded-full px-4 font-bold text-ink ring-1 ring-inset ring-ink/20 transition-colors hover:text-brand hover:ring-brand/50"
            >
              Sair
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-[1200px] px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
