import type { ReactNode } from 'react'

import { Brand } from '@/shared/ui/Brand'

interface AuthLayoutProps {
  title: string
  subtitle: string
  children: ReactNode
  footer: ReactNode
}

const HIGHLIGHTS = ['Registre pedidos em segundos', 'Acompanhe cada etapa da entrega', 'Toda a equipe vendo a mesma fila']

/** Moldura das telas de entrada: a proposta da operação à esquerda, o formulário à direita. */
export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <main className="min-h-dvh md:grid md:grid-cols-[minmax(0,1fr)_minmax(0,460px)] lg:grid-cols-[minmax(0,1fr)_520px]">
      <section className="flex flex-col gap-6 bg-paper px-6 py-6 md:justify-between md:gap-10 md:px-10 md:py-12 lg:px-14">
        <Brand />
        <div className="max-w-xl">
          {/* No celular fica só a frase principal, para o formulário aparecer sem rolar */}
          <p className="text-[24px] leading-[1.15] font-extrabold tracking-tight text-ink md:text-[34px] lg:text-[44px]">
            Gerencie seus pedidos de delivery <span className="text-brand">do recebimento à entrega</span>
          </p>
          <p className="mt-5 hidden text-[17px] leading-relaxed text-muted md:block">
            Cadastre pedidos, acompanhe o preparo e marque a entrega em uma única tela, com o status de cada pedido
            sempre atualizado.
          </p>
          <ul className="mt-8 hidden flex-col gap-3 md:flex">
            {HIGHLIGHTS.map((highlight) => (
              <li key={highlight} className="flex items-center gap-3 text-[15px] font-bold text-ink">
                <span aria-hidden="true" className="size-2 rounded-full bg-brand" />
                {highlight}
              </li>
            ))}
          </ul>
        </div>
        <p className="hidden text-[13px] text-muted md:block">Área do operador</p>
      </section>

      <section className="flex items-start justify-center bg-bench px-4 py-8 md:items-center md:px-8 md:py-10">
        <div className="w-full max-w-md rounded-[28px] border border-line bg-paper px-6 py-8 sm:px-8">
          <h1 className="text-[26px] font-extrabold tracking-tight">{title}</h1>
          <p className="mt-1 text-[15px] text-muted">{subtitle}</p>
          <div className="mt-6">{children}</div>
          <div className="card-divider mt-6 pt-5 text-[15px] text-muted">{footer}</div>
        </div>
      </section>
    </main>
  )
}
