import type { ReactNode } from 'react'

import { formatDateTime, formatMoney, formatOrderNumber } from '@/shared/lib/format'

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

/** A comanda impressa: o elemento central da interface. */
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
    <section aria-label="Comanda" className="ticket px-6 pt-5 pb-6 font-mono text-[13px] text-ink">
      <div className="flex items-baseline justify-between gap-4">
        <span className="text-[36px] leading-none font-semibold">
          {orderId ? formatOrderNumber(orderId) : '#----'}
        </span>
        <span className="text-right text-muted">{createdAt ? formatDateTime(createdAt) : 'novo pedido'}</span>
      </div>

      <div className="ticket-rule mt-4 pt-3">
        <p className="text-[15px] font-semibold">{customerName || 'Cliente'}</p>
        {customerPhone ? <p className="text-muted">{customerPhone}</p> : null}
        {addressLines.map((line) => (
          <p key={line} className="text-muted">
            {line}
          </p>
        ))}
      </div>

      <div className="ticket-rule mt-4 pt-3">
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
                  <td className="w-10 py-1 pr-2">{line.quantity}x</td>
                  <td className="py-1 pr-2 break-words">
                    <span className="block">{line.name}</span>
                    <span className="block text-muted">{formatMoney(line.unitPrice)} un.</span>
                  </td>
                  <td className="py-1 text-right whitespace-nowrap">{formatMoney(line.quantity * line.unitPrice)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="ticket-rule mt-4 flex items-baseline justify-between pt-3">
        <span className="text-[15px] font-semibold">Total</span>
        <span data-testid="receipt-total" className="text-[24px] font-semibold">
          {formatMoney(total)}
        </span>
      </div>

      {children}
    </section>
  )
}
