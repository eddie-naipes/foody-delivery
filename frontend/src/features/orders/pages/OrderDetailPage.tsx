import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router'

import { toApiError } from '@/shared/api/errors'
import { MESSAGES } from '@/shared/messages'
import { Alert } from '@/shared/ui/Alert'
import { Button } from '@/shared/ui/Button'
import { formatDateTime, formatOrderNumber } from '@/shared/lib/format'

import { addressLines } from '../address'
import { Receipt } from '../components/Receipt'
import { StatusBadge } from '../components/StatusBadge'
import { StatusRail } from '../components/StatusRail'
import { useOrder, useUpdateOrderStatus } from '../hooks'
import { availableTransitions, STATUS_META } from '../status'
import type { Order, OrderStatus } from '../types'

const backLink = (
  <Link to="/pedidos" className="text-[15px] text-muted underline-offset-4 hover:underline">
    Voltar para pedidos
  </Link>
)

export function OrderDetailPage() {
  const { id = '' } = useParams()
  const order = useOrder(id)

  if (order.isPending) {
    return (
      <div className="flex flex-col gap-4">
        {backLink}
        <p className="text-[15px] text-muted" aria-busy="true">
          Carregando pedido…
        </p>
      </div>
    )
  }

  if (order.isError) {
    return (
      <div className="flex max-w-md flex-col items-start gap-4">
        {backLink}
        <Alert tone="error">{order.error.message}</Alert>
      </div>
    )
  }

  return <OrderDetail order={order.data} />
}

function OrderDetail({ order }: { order: Order }) {
  const location = useLocation()
  const updateStatus = useUpdateOrderStatus(order.id)
  const [notice, setNotice] = useState<string | null>((location.state as { notice?: string } | null)?.notice ?? null)
  const [error, setError] = useState<string | null>(null)
  const [confirmingCancel, setConfirmingCancel] = useState(false)
  const navigate = useNavigate()

  // O aviso veio no estado do histórico: limpa para não reaparecer ao recarregar a página
  useEffect(() => {
    if (location.state) navigate(location.pathname, { replace: true, state: null })
  }, [location.pathname, location.state, navigate])

  const transitions = availableTransitions(order.status)
  const advance = transitions.find((status) => status !== 'CANCELADO')
  const canCancel = transitions.includes('CANCELADO')

  async function changeStatus(status: OrderStatus) {
    setNotice(null)
    setError(null)
    setConfirmingCancel(false)
    try {
      await updateStatus.mutateAsync(status)
      setNotice(STATUS_META[status].done)
    } catch (cause) {
      const apiError = toApiError(cause)
      setError(apiError.status === 422 ? MESSAGES.orderChangedElsewhere : apiError.message)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        {backLink}
        <div className="mt-2 flex flex-wrap items-center gap-3">
          <h1 className="text-[36px] leading-tight font-bold tracking-tight">Pedido {formatOrderNumber(order.id)}</h1>
          <StatusBadge status={order.status} />
        </div>
        <p className="text-[15px] text-muted">
          Criado por {order.createdBy.name} em {formatDateTime(order.createdAt)}
        </p>
      </div>

      <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,1fr)_380px]">
        <section aria-labelledby="andamento" className="flex flex-col gap-6">
          <h2 id="andamento" className="text-[18px] font-bold">
            Andamento
          </h2>
          <StatusRail status={order.status} />

          {notice ? <Alert tone="success">{notice}</Alert> : null}
          {error ? <Alert tone="error">{error}</Alert> : null}

          {transitions.length === 0 ? (
            // Cancelado já é explicado pelo trilho; só a entrega concluída ganha uma linha própria
            order.status === 'ENTREGUE' ? (
              <p className="text-[15px] text-muted">Pedido concluído. Não há mais etapas.</p>
            ) : null
          ) : confirmingCancel ? (
            <div className="flex flex-col gap-3 rounded-md bg-paper p-4 ring-1 ring-inset ring-danger/30">
              <p className="text-[15px]">
                Cancelar o pedido {formatOrderNumber(order.id)}? Depois de cancelado, ele não pode voltar ao fluxo.
              </p>
              <div className="flex flex-wrap gap-3">
                <Button variant="danger" loading={updateStatus.isPending} onClick={() => void changeStatus('CANCELADO')}>
                  Confirmar cancelamento
                </Button>
                <Button variant="ghost" onClick={() => setConfirmingCancel(false)}>
                  Manter pedido
                </Button>
              </div>
            </div>
          ) : (
            <div className="flex flex-wrap gap-3">
              {advance ? (
                <Button loading={updateStatus.isPending} onClick={() => void changeStatus(advance)}>
                  {STATUS_META[advance].action}
                </Button>
              ) : null}
              {canCancel ? (
                <Button variant="secondary" disabled={updateStatus.isPending} onClick={() => setConfirmingCancel(true)}>
                  {STATUS_META.CANCELADO.action}
                </Button>
              ) : null}
            </div>
          )}
        </section>

        <Receipt
          orderId={order.id}
          createdAt={order.createdAt}
          customerName={order.customerName}
          customerPhone={order.customerPhone}
          addressLines={addressLines(order.deliveryAddress)}
          lines={order.items.map((item) => ({ key: item.id, ...item }))}
          total={order.totalAmount}
        />
      </div>
    </div>
  )
}
