/**
 * Máscaras aplicadas enquanto se digita: o campo nunca chega a conter um caractere
 * que não faz sentido para ele (letra em telefone, CEP, quantidade ou preço).
 */

const onlyDigits = (value: string) => value.replace(/\D/g, '')

/** Telefone brasileiro com DDD: (11) 3333-4444 ou (11) 99999-8888. */
export function maskPhone(value: string): string {
  const digits = onlyDigits(value).slice(0, 11)
  if (digits.length === 0) return ''
  if (digits.length <= 2) return `(${digits}`
  const ddd = digits.slice(0, 2)
  const number = digits.slice(2)
  if (number.length <= 4) return `(${ddd}) ${number}`
  const split = digits.length === 11 ? 5 : 4
  return `(${ddd}) ${number.slice(0, split)}-${number.slice(split)}`
}

/** CEP: 01310-100. */
export function maskZipCode(value: string): string {
  const digits = onlyDigits(value).slice(0, 8)
  return digits.length > 5 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : digits
}

export function maskDigits(value: string, maxLength: number): string {
  return onlyDigits(value).slice(0, maxLength)
}

/** Valor em reais digitado com vírgula: "45,90". Aceita ponto como vírgula e corta após 2 casas. */
export function maskMoney(value: string): string {
  const normalized = value.replace(/\./g, ',')
  const separator = normalized.indexOf(',')
  if (separator < 0) return onlyDigits(normalized).slice(0, 8)
  const integer = onlyDigits(normalized.slice(0, separator)).slice(0, 8) || '0'
  const decimals = onlyDigits(normalized.slice(separator + 1)).slice(0, 2)
  return `${integer},${decimals}`
}
