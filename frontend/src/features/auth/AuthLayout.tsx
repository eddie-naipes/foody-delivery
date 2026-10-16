import type { ReactNode } from 'react'

interface AuthLayoutProps {
  title: string
  subtitle: string
  children: ReactNode
  footer: ReactNode
}

/** Moldura das telas de entrada: a marca à esquerda, o formulário numa comanda à direita. */
export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <main className="min-h-dvh md:grid md:grid-cols-[1fr_minmax(0,460px)]">
      <section className="flex flex-col justify-between bg-ink px-6 py-8 text-paper md:px-12 md:py-12">
        <p className="text-[18px] font-bold tracking-tight">Foody Expedição</p>
        <div className="mt-10 max-w-md md:mt-0">
          <p className="text-[36px] leading-[1.1] font-bold tracking-tight">
            Cada pedido, da comanda à porta do cliente.
          </p>
          <p className="mt-4 text-[15px] text-paper/70">
            Registre pedidos, acompanhe o preparo e marque a entrega sem perder nenhuma comanda.
          </p>
        </div>
        <span aria-hidden="true" className="mt-10 hidden h-1.5 w-16 rounded-full bg-ticket md:block" />
      </section>

      <section className="flex items-center justify-center px-4 py-10 md:px-10">
        <div className="ticket w-full max-w-sm px-6 py-8">
          <h1 className="text-[24px] font-bold tracking-tight">{title}</h1>
          <p className="mt-1 text-[15px] text-muted">{subtitle}</p>
          <div className="mt-6">{children}</div>
          <div className="ticket-rule mt-6 pt-4 text-[15px] text-muted">{footer}</div>
        </div>
      </section>
    </main>
  )
}
