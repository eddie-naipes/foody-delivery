import { useState } from 'react'
import { Link } from 'react-router'

import { cn } from '@/shared/lib/cn'
import { Alert } from '@/shared/ui/Alert'
import { Button, buttonBase, buttonStyles } from '@/shared/ui/Button'

import { OrderTicket } from '../components/OrderTicket'
import { StatusFilter, type StatusFilterValue } from '../components/StatusFilter'
import { useOrders } from '../hooks'
import { STATUS_META } from '../status'

const newOrderLink = cn(buttonBase, buttonStyles.primary)

export function OrdersPage() {
  const orders = useOrders()
  const [filter, setFilter] = useState<StatusFilterValue>('TODOS')

  const all = orders.data ?? []
  const visible = filter === 'TODOS' ? all : all.filter((order) => order.status === filter)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-[34px] leading-tight font-extrabold tracking-tight">Pedidos</h1>
          <p className="text-[15px] text-muted">Do mais recente para o mais antigo. A lista se atualiza sozinha.</p>
        </div>
        {all.length > 0 ? (
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
      ) : all.length === 0 ? (
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
          <StatusFilter orders={all} value={filter} onChange={setFilter} />
          {visible.length === 0 ? (
            <p className="text-[15px] text-muted">
              Nenhum pedido {filter === 'TODOS' ? '' : STATUS_META[filter].label.toLowerCase()}.
            </p>
          ) : (
            <ul className="grid gap-x-5 gap-y-7 sm:grid-cols-2 lg:grid-cols-3">
              {visible.map((order) => (
                <li key={order.id}>
                  <OrderTicket order={order} />
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  )
}
