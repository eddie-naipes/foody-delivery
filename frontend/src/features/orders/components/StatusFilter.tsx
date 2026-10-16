import { cn } from '@/shared/lib/cn'

import { ORDER_STATUSES, STATUS_META } from '../status'
import type { Order, OrderStatus } from '../types'

export type StatusFilterValue = OrderStatus | 'TODOS'

interface StatusFilterProps {
  orders: Order[]
  value: StatusFilterValue
  onChange: (value: StatusFilterValue) => void
}

/** Contagem por status que também serve de filtro: a fila de relance. */
export function StatusFilter({ orders, value, onChange }: StatusFilterProps) {
  const options: Array<{ value: StatusFilterValue; label: string; count: number; dot?: string }> = [
    { value: 'TODOS', label: 'Todos', count: orders.length },
    ...ORDER_STATUSES.map((status) => ({
      value: status,
      label: STATUS_META[status].label,
      count: orders.filter((order) => order.status === status).length,
      dot: STATUS_META[status].dot,
    })),
  ]

  return (
    <div role="group" aria-label="Filtrar por status" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1">
      {options.map((option) => {
        const active = option.value === value
        return (
          <button
            key={option.value}
            type="button"
            aria-pressed={active}
            onClick={() => onChange(option.value)}
            className={cn(
              'inline-flex h-9 shrink-0 items-center gap-2 rounded-full px-3.5 text-[15px] font-medium transition-colors',
              active ? 'bg-ink text-paper' : 'bg-paper text-ink ring-1 ring-inset ring-ink/15 hover:ring-ink/35',
            )}
          >
            {option.dot ? <span aria-hidden="true" className={cn('size-2 rounded-full', option.dot)} /> : null}
            {option.label}
            <span className={cn('font-mono text-[13px]', active ? 'text-paper/80' : 'text-muted')}>{option.count}</span>
          </button>
        )
      })}
    </div>
  )
}
