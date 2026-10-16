import { Link } from 'react-router'

import { formatMoney, formatOrderNumber, formatRelativeTime } from '@/shared/lib/format'

import type { Order } from '../types'
import { StatusBadge } from './StatusBadge'

/** Comanda resumida na fila de pedidos. A comanda inteira é o link para o detalhe. */
export function OrderTicket({ order }: { order: Order }) {
  const itemCount = order.items.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <Link
      to={`/pedidos/${order.id}`}
      className="ticket group block px-5 pt-4 pb-5 transition-transform hover:-translate-y-0.5"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <span className="font-mono text-[24px] leading-none font-semibold">{formatOrderNumber(order.id)}</span>
          <p className="mt-1 text-[13px] text-muted">
            <time dateTime={order.createdAt}>{formatRelativeTime(order.createdAt)}</time>
          </p>
        </div>
        <StatusBadge status={order.status} />
      </div>

      <p className="mt-4 truncate text-[18px] font-semibold group-hover:underline">{order.customerName}</p>
      <p className="truncate text-[15px] text-muted">{order.deliveryAddress.neighborhood}</p>

      <div className="ticket-rule mt-4 flex items-end justify-between pt-3 font-mono text-[13px]">
        <span className="text-muted">
          {itemCount} {itemCount === 1 ? 'item' : 'itens'}
        </span>
        <span className="text-[15px] font-semibold">{formatMoney(order.totalAmount)}</span>
      </div>
    </Link>
  )
}
