import { cn } from '@/shared/lib/cn'

import { STATUS_META } from '../status'
import type { OrderStatus } from '../types'

/** Status sempre com cor e texto — nunca só a cor. */
export function StatusBadge({ status, className }: { status: OrderStatus; className?: string }) {
  const meta = STATUS_META[status]
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[13px] font-semibold',
        meta.tone,
        className,
      )}
    >
      <span aria-hidden="true" className={cn('size-2 rounded-full', meta.dot)} />
      {meta.label}
    </span>
  )
}
