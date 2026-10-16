import type { Address } from './types'

export function formatZipCode(zipCode: string): string {
  const digits = zipCode.replace(/\D/g, '')
  return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : zipCode
}

/** Endereço em linhas, como sai impresso na comanda. Ignora partes ainda não preenchidas. */
export function addressLines(address: Partial<Address>): string[] {
  const street = [address.street, address.number].filter(Boolean).join(', ')
  const firstLine = [street, address.complement].filter(Boolean).join(' - ')
  const secondLine = [address.neighborhood, address.city].filter(Boolean).join(', ')
  const zip = address.zipCode ? `CEP ${formatZipCode(address.zipCode)}` : ''
  return [firstLine, secondLine, zip].filter(Boolean)
}
