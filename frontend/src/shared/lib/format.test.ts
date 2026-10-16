import { describe, expect, it } from 'vitest'

import { formatMoney, formatOrderNumber, formatRelativeTime, parseMoney } from './format'

describe('formatação', () => {
  it('formata moeda em reais', () => {
    expect(formatMoney(99.9).replace(/\s/g, ' ')).toBe('R$ 99,90')
  })

  it('formata o número da comanda com quatro dígitos', () => {
    expect(formatOrderNumber(42)).toBe('#0042')
    expect(formatOrderNumber(12345)).toBe('#12345')
  })

  it('lê valores digitados com vírgula ou ponto', () => {
    expect(parseMoney('45,90')).toBe(45.9)
    expect(parseMoney('45.9')).toBe(45.9)
    expect(parseMoney(' 1.234,50 ')).toBe(1234.5)
    expect(parseMoney('')).toBeNaN()
    expect(parseMoney('abc')).toBeNaN()
  })

  it('mostra há quanto tempo em português', () => {
    const now = new Date('2026-10-16T12:00:00Z')
    expect(formatRelativeTime('2026-10-16T11:59:40Z', now)).toBe('agora')
    expect(formatRelativeTime('2026-10-16T11:55:00Z', now)).toBe('há 5 minutos')
    expect(formatRelativeTime('2026-10-16T09:00:00Z', now)).toBe('há 3 horas')
    expect(formatRelativeTime('2026-10-14T12:00:00Z', now)).toBe('há 2 dias')
  })
})
