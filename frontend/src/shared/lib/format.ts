const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const relative = new Intl.RelativeTimeFormat('pt-BR', { numeric: 'always' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function formatMoney(value: number): string {
  return money.format(value)
}

export function formatOrderNumber(id: number): string {
  return `#${String(id).padStart(4, '0')}`
}

export function formatDateTime(iso: string): string {
  return dateTime.format(new Date(iso))
}

/** Aceita "45,90", "45.9" e "1.234,50" (padrão brasileiro). Devolve NaN se não for número. */
export function parseMoney(input: string): number {
  const value = input.trim()
  if (value === '') return Number.NaN
  const normalized = value.includes(',') ? value.replace(/\./g, '').replace(',', '.') : value
  return /^\d+(\.\d+)?$/.test(normalized) ? Number(normalized) : Number.NaN
}

const UNITS: Array<[Intl.RelativeTimeFormatUnit, number]> = [
  ['day', 86_400],
  ['hour', 3_600],
  ['minute', 60],
]

export function formatRelativeTime(iso: string, now: Date = new Date()): string {
  const seconds = Math.round((new Date(iso).getTime() - now.getTime()) / 1000)
  for (const [unit, size] of UNITS) {
    if (Math.abs(seconds) >= size) return relative.format(Math.round(seconds / size), unit)
  }
  return 'agora'
}
