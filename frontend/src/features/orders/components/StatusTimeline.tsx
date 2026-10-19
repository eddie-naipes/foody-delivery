import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/format'

import { STATUS_META } from '../status'
import type { StatusChange } from '../types'

/** Linha do tempo do pedido: cada mudança de status, quando aconteceu e quem fez. */
export function StatusTimeline({ history }: { history: StatusChange[] }) {
  return (
    <section aria-labelledby="historico" className="flex flex-col gap-3">
      <h3 id="historico" className="text-[17px] font-extrabold">
        Histórico
      </h3>
      <ol className="flex flex-col">
        {history.map((change, index) => {
          const last = index === history.length - 1
          return (
            <li key={`${change.status}-${change.changedAt}`} className="relative flex gap-3 pb-4 last:pb-0">
              {/* Trilho vertical ligando as etapas */}
              {last ? null : <span aria-hidden="true" className="absolute top-4 bottom-0 left-[5px] w-px bg-line" />}
              <span
                aria-hidden="true"
                className={cn('relative mt-1.5 size-[11px] shrink-0 rounded-full', STATUS_META[change.status].dot)}
              />
              <div className="flex flex-col">
                <span className={cn('text-[15px]', last ? 'font-extrabold text-ink' : 'font-bold text-ink')}>
                  {STATUS_META[change.status].label}
                </span>
                <span className="text-[13px] text-muted tabular-nums">
                  <time dateTime={change.changedAt}>{formatDateTime(change.changedAt)}</time>
                  {change.changedBy ? ` por ${change.changedBy.name}` : null}
                </span>
              </div>
            </li>
          )
        })}
      </ol>
    </section>
  )
}
