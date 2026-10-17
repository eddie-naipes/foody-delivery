import type { ReactNode } from 'react'

import { formatDateTime, formatMoney, formatOrderNumber, formatPhone } from '@/shared/lib/format'

export interface ReceiptLine {
  key: string | number
  name: string
  quantity: number
  unitPrice: number
}

interface ReceiptProps {
  /** Sem número enquanto o pedido ainda não foi criado. */
  orderId?: number
  createdAt?: string
  customerName: string
  customerPhone?: string
  addressLines: string[]
  lines: ReceiptLine[]
  total: number
  /** Anima a entrada de cada linha nova (pré-visualização ao vivo). */
  animateLines?: boolean
  children?: ReactNode
}

/** Resumo do pedido: cliente, onde entregar, itens e total. */
export function Receipt({
  orderId,
  createdAt,
  customerName,
  customerPhone,
  addressLines,
  lines,
  total,
  animateLines = false,
  children,
}: ReceiptProps) {
  return (
    <section aria-label="Resumo do pedido" className="card p-6 text-[15px] text-ink">
      <div className="flex items-baseline justify-between gap-4">
        <span className="text-[30px] leading-none font-extrabold text-brand tabular-nums">
          {orderId ? formatOrderNumber(orderId) : 'Novo pedido'}
        </span>
        {createdAt ? <span className="text-right text-[13px] text-muted">{formatDateTime(createdAt)}</span> : null}
      </div>

      <dl className="card-divider mt-5 flex flex-col gap-3 pt-4">
        <div>
          <dt className="font-bold">Cliente:</dt>
          <dd className="text-muted">
            {customerName || 'Ainda não informado'}
            {customerPhone ? <span className="block">{formatPhone(customerPhone)}</span> : null}
          </dd>
        </div>
        <div>
          <dt className="font-bold">Entregar em:</dt>
          <dd className="text-muted">
            {addressLines.length === 0
              ? 'Ainda não informado'
              : addressLines.map((line) => (
                  <span key={line} className="block">
                    {line}
                  </span>
                ))}
          </dd>
        </div>
      </dl>

      <div className="card-divider mt-5 pt-4">
        {lines.length === 0 ? (
          <p className="text-muted">Os itens aparecem aqui conforme você digita.</p>
        ) : (
          <table className="w-full border-collapse">
            <caption className="sr-only">Itens do pedido</caption>
            <thead className="sr-only">
              <tr>
                <th scope="col">Quantidade</th>
                <th scope="col">Item</th>
                <th scope="col">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {lines.map((line) => (
                <tr key={line.key} className={animateLines ? 'print-in align-top' : 'align-top'}>
                  <td className="w-10 py-1.5 pr-2 font-bold text-brand tabular-nums">{line.quantity}x</td>
                  <td className="py-1.5 pr-2 break-words">
                    <span className="block font-bold">{line.name}</span>
                    <span className="block text-[13px] text-muted">{formatMoney(line.unitPrice)} cada</span>
                  </td>
                  <td className="py-1.5 text-right whitespace-nowrap tabular-nums">
                    {formatMoney(line.quantity * line.unitPrice)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="mt-5 flex items-baseline justify-between rounded-2xl bg-blush px-4 py-3">
        <span className="font-extrabold">Total</span>
        <span data-testid="receipt-total" className="text-[24px] font-extrabold text-brand tabular-nums">
          {formatMoney(total)}
        </span>
      </div>

      {children}
    </section>
  )
}
