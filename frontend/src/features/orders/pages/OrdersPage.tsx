import { useEffect } from 'react'
import { Link, useSearchParams } from 'react-router'

import { cn } from '@/shared/lib/cn'
import { Alert } from '@/shared/ui/Alert'
import { Button, buttonBase, buttonStyles } from '@/shared/ui/Button'

import { OrderTicket } from '../components/OrderTicket'
import { Pagination } from '../components/Pagination'
import { StatusFilter, type StatusFilterValue } from '../components/StatusFilter'
import { useOrders, useStatusCounts } from '../hooks'
import { ORDER_STATUSES, STATUS_META } from '../status'
import type { OrderStatus } from '../types'

const newOrderLink = cn(buttonBase, buttonStyles.primary)

/**
 * Filtro e página moram na URL (?status=ENTREGUE&pagina=2): o voltar do navegador
 * funciona e o endereço pode ser compartilhado. Valores inválidos caem no padrão.
 */
function readListState(params: URLSearchParams): { status?: OrderStatus; page: number } {
  const status = params.get('status')
  const page = Number(params.get('pagina'))
  return {
    status: ORDER_STATUSES.includes(status as OrderStatus) ? (status as OrderStatus) : undefined,
    page: Number.isInteger(page) && page >= 1 ? page : 1,
  }
}

function toSearchParams(status: OrderStatus | undefined, page: number): URLSearchParams {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (page > 1) params.set('pagina', String(page))
  return params
}

export function OrdersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const { status, page } = readListState(searchParams)
  const orders = useOrders({ status, page: page - 1 })
  const counts = useStatusCounts()

  function changeFilter(value: StatusFilterValue) {
    setSearchParams(toSearchParams(value === 'TODOS' ? undefined : value, 1))
  }

  function changePage(next: number) {
    setSearchParams(toSearchParams(status, next))
    window.scrollTo(0, 0)
  }

  // Página que deixou de existir (pedidos mudaram de status, link antigo): vai para a última
  const totalPages = orders.data?.totalPages ?? 0
  useEffect(() => {
    if (!orders.isPlaceholderData && totalPages > 0 && page > totalPages) {
      setSearchParams(toSearchParams(status, totalPages), { replace: true })
    }
  }, [orders.isPlaceholderData, totalPages, page, status, setSearchParams])

  const hasAnyOrder = (counts.data?.total ?? orders.data?.totalElements ?? 0) > 0
  const visible = orders.data?.content ?? []

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-[34px] leading-tight font-extrabold tracking-tight">Pedidos</h1>
          <p className="text-[15px] text-muted">Do mais recente para o mais antigo. A lista se atualiza sozinha.</p>
        </div>
        {hasAnyOrder ? (
          <Link to="/pedidos/novo" className={newOrderLink}>
            Novo pedido
          </Link>
        ) : null}
      </div>

      {orders.isPending ? (
        <p className="text-[15px] text-muted" aria-busy="true">
          Carregando pedidos…
        </p>
      ) : orders.isError ? (
        <div className="flex flex-col items-start gap-3">
          <Alert tone="error">{orders.error.message}</Alert>
          <Button variant="secondary" onClick={() => void orders.refetch()} loading={orders.isFetching}>
            Tentar de novo
          </Button>
        </div>
      ) : !hasAnyOrder ? (
        <div className="card max-w-md p-8">
          <h2 className="text-[24px] font-extrabold tracking-tight">Nenhum pedido por aqui ainda</h2>
          <p className="mt-2 text-[15px] text-muted">
            Os pedidos que você criar aparecem nesta lista, cada um com o status atual.
          </p>
          <Link to="/pedidos/novo" className={cn(newOrderLink, 'mt-6')}>
            Criar primeiro pedido
          </Link>
        </div>
      ) : (
        <>
          <StatusFilter counts={counts.data} value={status ?? 'TODOS'} onChange={changeFilter} />
          {visible.length === 0 ? (
            <p className="text-[15px] text-muted">
              Nenhum pedido {status ? STATUS_META[status].label.toLowerCase() : ''}.
            </p>
          ) : (
            <ul
              aria-busy={orders.isPlaceholderData || undefined}
              className={cn(
                'grid gap-x-5 gap-y-7 transition-opacity sm:grid-cols-2 lg:grid-cols-3',
                orders.isPlaceholderData && 'opacity-60',
              )}
            >
              {visible.map((order) => (
                <li key={order.id}>
                  <OrderTicket order={order} />
                </li>
              ))}
            </ul>
          )}
          <Pagination
            page={page}
            totalPages={totalPages}
            totalItems={orders.data?.totalElements ?? 0}
            onChange={changePage}
          />
        </>
      )}
    </div>
  )
}
