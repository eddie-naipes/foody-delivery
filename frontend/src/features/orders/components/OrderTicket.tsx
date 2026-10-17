import { Link } from 'react-router'

import { formatMoney, formatOrderNumber, formatRelativeTime } from '@/shared/lib/format'

import type { Order } from '../types'
import { StatusBadge } from './StatusBadge'

/** Pedido na fila: número em destaque, cliente, onde entregar e total. O cartão inteiro abre o detalhe. */
export function OrderTicket({ order }: { order: Order }) {
  const itemCount = order.items.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <Link
      to={`/pedidos/${order.id}`}
      className="card group block p-5 transition-[border-color,box-shadow] hover:border-coral/60 hover:shadow-[0_16px_32px_-20px_rgb(211_1_1/0.45)]"
    >
      <div className="flex items-start justify-between gap-3">
        <StatusBadge status={order.status} />
        <span className="text-[22px] leading-none font-extrabold text-brand tabular-nums">
          {formatOrderNumber(order.id)}
        </span>
      </div>

      <p className="mt-4 truncate text-[18px] font-extrabold group-hover:text-brand">{order.customerName}</p>
      <p className="mt-1 truncate text-[15px] text-muted">
        <span className="font-bold text-ink">Entregar em:</span> {order.deliveryAddress.neighborhood}
      </p>
      <p className="mt-1 text-[13px] text-muted">
        <time dateTime={order.createdAt}>{formatRelativeTime(order.createdAt)}</time>
      </p>

      <div className="card-divider mt-4 flex items-center justify-between pt-3">
        <span className="text-[14px] text-muted">
          {itemCount} {itemCount === 1 ? 'item' : 'itens'}
        </span>
        <span className="text-[17px] font-extrabold tabular-nums">{formatMoney(order.totalAmount)}</span>
      </div>
    </Link>
  )
}
