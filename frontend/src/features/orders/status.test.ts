import { describe, expect, it } from 'vitest'

import { availableTransitions, canTransition, ORDER_STATUSES, STATUS_META } from './status'
import type { OrderStatus } from './types'

// Mesma tabela do OrderStatusTest do back-end: o front só oferece o que a API aceita
const ALLOWED: Array<[OrderStatus, OrderStatus]> = [
  ['RECEBIDO', 'EM_PREPARO'],
  ['RECEBIDO', 'CANCELADO'],
  ['EM_PREPARO', 'SAIU_PARA_ENTREGA'],
  ['EM_PREPARO', 'CANCELADO'],
  ['SAIU_PARA_ENTREGA', 'ENTREGUE'],
  ['SAIU_PARA_ENTREGA', 'CANCELADO'],
]

describe('status do pedido', () => {
  it.each(ORDER_STATUSES.flatMap((from) => ORDER_STATUSES.map((to) => [from, to] as const)))(
    '%s -> %s segue a regra do back-end',
    (from, to) => {
      const expected = ALLOWED.some(([f, t]) => f === from && t === to)
      expect(canTransition(from, to)).toBe(expected)
    },
  )

  it('lista as ações possíveis com o avanço antes do cancelamento', () => {
    expect(availableTransitions('RECEBIDO')).toEqual(['EM_PREPARO', 'CANCELADO'])
    expect(availableTransitions('SAIU_PARA_ENTREGA')).toEqual(['ENTREGUE', 'CANCELADO'])
    expect(availableTransitions('ENTREGUE')).toEqual([])
    expect(availableTransitions('CANCELADO')).toEqual([])
  })

  it('tem rótulo e ação legíveis para cada status', () => {
    for (const status of ORDER_STATUSES) {
      expect(STATUS_META[status].label).not.toBe('')
      expect(STATUS_META[status].action).not.toBe('')
    }
  })
})
