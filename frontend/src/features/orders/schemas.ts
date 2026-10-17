import { z } from 'zod'

import { parseMoney } from '@/shared/lib/format'
import { MESSAGES } from '@/shared/messages'

import type { CreateOrderInput } from './types'

/**
 * Regras do formulário de pedido — as mesmas do back-end, para o operador saber do
 * erro antes de enviar. Os campos numéricos ficam como texto enquanto se digita
 * ("45,90") e são convertidos só no envio.
 */
const required = (message: string) => z.string().trim().min(1, message)

export const itemSchema = z.object({
  name: required(MESSAGES.itemNameRequired),
  quantity: z.string().refine((value) => /^\d+$/.test(value.trim()) && Number(value) >= 1, MESSAGES.quantityInvalid),
  unitPrice: z
    .string()
    .refine((value) => parseMoney(value) > 0, MESSAGES.unitPriceInvalid)
    .refine((value) => Number.isNaN(parseMoney(value)) || hasAtMostTwoDecimals(value), MESSAGES.unitPriceDecimals),
})

export const orderFormSchema = z.object({
  customerName: required(MESSAGES.customerNameRequired),
  customerPhone: required(MESSAGES.customerPhoneRequired).refine(isPhone, MESSAGES.customerPhoneInvalid),
  deliveryAddress: z.object({
    street: required(MESSAGES.streetRequired),
    number: required(MESSAGES.numberRequired),
    complement: z.string(),
    neighborhood: required(MESSAGES.neighborhoodRequired),
    city: required(MESSAGES.cityRequired),
    zipCode: z.string().trim().regex(/^\d{5}-?\d{3}$/, MESSAGES.zipCodeInvalid),
  }),
  items: z.array(itemSchema).min(1, MESSAGES.itemsRequired),
})

export type OrderFormValues = z.input<typeof orderFormSchema>

/** Telefone com DDD: só dígitos e pontuação de telefone, com 10 ou 11 dígitos. */
function isPhone(value: string): boolean {
  const digits = value.replace(/\D/g, '')
  return /^[\d\s()+-]*$/.test(value) && (digits.length === 10 || digits.length === 11)
}

function hasAtMostTwoDecimals(value: string): boolean {
  return Math.round(parseMoney(value) * 100) === parseMoney(value) * 100
}

export function emptyItem() {
  return { name: '', quantity: '1', unitPrice: '' }
}

export function emptyOrderForm(): OrderFormValues {
  return {
    customerName: '',
    customerPhone: '',
    deliveryAddress: { street: '', number: '', complement: '', neighborhood: '', city: '', zipCode: '' },
    items: [emptyItem()],
  }
}

export function toCreateOrderInput(values: OrderFormValues): CreateOrderInput {
  const { deliveryAddress: address } = values
  return {
    customerName: values.customerName.trim(),
    customerPhone: values.customerPhone.replace(/\D/g, ''),
    deliveryAddress: {
      street: address.street.trim(),
      number: address.number.trim(),
      complement: address.complement.trim() || null,
      neighborhood: address.neighborhood.trim(),
      city: address.city.trim(),
      zipCode: address.zipCode.trim(),
    },
    items: values.items.map((item) => ({
      name: item.name.trim(),
      quantity: Number(item.quantity),
      unitPrice: parseMoney(item.unitPrice),
    })),
  }
}
