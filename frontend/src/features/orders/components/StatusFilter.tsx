import { cn } from '@/shared/lib/cn'

import { ORDER_STATUSES, STATUS_META } from '../status'
import type { OrderStatus, StatusCounts } from '../types'

export type StatusFilterValue = OrderStatus | 'TODOS'

interface StatusFilterProps {
  /** Contagens vindas da API; enquanto não chegam, os botões aparecem sem número. */
  counts: StatusCounts | undefined
  value: StatusFilterValue
  onChange: (value: StatusFilterValue) => void
}

/** Contagem por status que também serve de filtro: a fila de relance. */
export function StatusFilter({ counts, value, onChange }: StatusFilterProps) {
  const options: Array<{ value: StatusFilterValue; label: string; count?: number; dot?: string }> = [
    { value: 'TODOS', label: 'Todos', count: counts?.total },
    ...ORDER_STATUSES.map((status) => ({
      value: status,
      label: STATUS_META[status].label,
      count: counts?.byStatus[status],
      dot: STATUS_META[status].dot,
    })),
  ]

  return (
    <div role="group" aria-label="Filtrar por status" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
      {options.map((option) => {
        const active = option.value === value
        return (
          <button
            key={option.value}
            type="button"
            aria-pressed={active}
            onClick={() => onChange(option.value)}
            className={cn(
              'inline-flex h-10 shrink-0 items-center gap-2 rounded-full px-4 text-[15px] font-bold transition-colors',
              active ? 'bg-brand text-paper shadow-brand' : 'bg-paper text-ink ring-1 ring-inset ring-line hover:ring-coral/60',
            )}
          >
            {option.dot ? <span aria-hidden="true" className={cn('size-2 rounded-full', option.dot)} /> : null}
            {option.label}
            {option.count === undefined ? null : (
              <span className={cn('text-[13px] tabular-nums', active ? 'text-paper/85' : 'text-muted')}>{option.count}</span>
            )}
          </button>
        )
      })}
    </div>
  )
}
