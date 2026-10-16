import { Link, Outlet } from 'react-router'

import { useAuth, useSessionUser } from '@/features/auth/useAuth'

/** Moldura das telas logadas: barra fina com a marca, o operador e a saída. */
export function AppLayout() {
  const { logout } = useAuth()
  const user = useSessionUser()

  return (
    <div className="min-h-dvh">
      <header className="bg-ink text-paper">
        <div className="mx-auto flex h-14 max-w-[1200px] items-center justify-between gap-4 px-4">
          <Link to="/pedidos" className="text-[18px] font-bold tracking-tight">
            Foody Expedição
          </Link>
          <div className="flex items-center gap-3 text-[15px]">
            <span className="max-w-[40vw] truncate text-paper/80">{user.name}</span>
            <button
              type="button"
              onClick={logout}
              className="rounded-md px-3 py-1.5 font-semibold ring-1 ring-inset ring-paper/30 hover:bg-paper/10"
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
