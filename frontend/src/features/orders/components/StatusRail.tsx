import { cn } from '@/shared/lib/cn'

import { DELIVERY_STEPS, STATUS_META } from '../status'
import type { OrderStatus } from '../types'

/** Etapas do pedido em sequência, com a atual destacada. */
export function StatusRail({ status }: { status: OrderStatus }) {
  const canceled = status === 'CANCELADO'
  const currentIndex = DELIVERY_STEPS.indexOf(status)

  return (
    <div>
      <ol className="grid grid-cols-4 gap-2">
        {DELIVERY_STEPS.map((step, index) => {
          const done = !canceled && index < currentIndex
          const current = !canceled && index === currentIndex
          return (
            <li key={step} aria-current={current ? 'step' : undefined} className="flex flex-col gap-2">
              <span
                aria-hidden="true"
                className={cn(
                  'h-2 rounded-full',
                  done || current ? STATUS_META[status].dot : 'bg-ink/15',
                  canceled && 'bg-ink/10',
                )}
              />
              <span
                className={cn(
                  'text-[13px] leading-tight',
                  current ? 'font-extrabold text-ink' : done ? 'font-bold text-ink' : 'text-muted',
                )}
              >
                {index + 1}. {STATUS_META[step].label}
              </span>
            </li>
          )
        })}
      </ol>
      {canceled ? (
        <p className="mt-3 text-[15px] font-semibold text-status-canceled">Este pedido foi cancelado.</p>
      ) : null}
    </div>
  )
}
