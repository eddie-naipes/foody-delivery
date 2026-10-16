import { describe, expect, it } from 'vitest'

import { emptyOrderForm, orderFormSchema, toCreateOrderInput, type OrderFormValues } from './schemas'

function validForm(): OrderFormValues {
  return {
    customerName: ' Maria Silva ',
    customerPhone: '11999998888',
    deliveryAddress: {
      street: 'Av. Paulista',
      number: '1000',
      complement: '',
      neighborhood: 'Bela Vista',
      city: 'São Paulo',
      zipCode: '01310-100',
    },
    items: [
      { name: 'Pizza', quantity: '2', unitPrice: '45,00' },
      { name: 'Refrigerante', quantity: '1', unitPrice: '9.90' },
    ],
  }
}

function errorPaths(values: OrderFormValues) {
  const result = orderFormSchema.safeParse(values)
  return result.success ? [] : result.error.issues.map((issue) => issue.path.join('.'))
}

describe('formulário de pedido', () => {
  it('aceita um pedido válido', () => {
    expect(errorPaths(validForm())).toEqual([])
  })

  it('começa com um item em branco', () => {
    expect(emptyOrderForm().items).toHaveLength(1)
  })

  it.each(['01310-100', '01310100'])('aceita o CEP %s', (zipCode) => {
    const form = validForm()
    form.deliveryAddress.zipCode = zipCode
    expect(errorPaths(form)).toEqual([])
  })

  it.each(['0131-0100', '1234567', 'abcde-fgh'])('rejeita o CEP %s', (zipCode) => {
    const form = validForm()
    form.deliveryAddress.zipCode = zipCode
    expect(errorPaths(form)).toEqual(['deliveryAddress.zipCode'])
  })

  it('exige ao menos um item', () => {
    expect(errorPaths({ ...validForm(), items: [] })).toEqual(['items'])
  })

  it('valida quantidade e preço de cada item', () => {
    const form = validForm()
    form.items = [{ name: '', quantity: '0', unitPrice: '9,999' }]
    expect(errorPaths(form)).toEqual(['items.0.name', 'items.0.quantity', 'items.0.unitPrice'])
  })

  it('converte o formulário no corpo esperado pela API', () => {
    expect(toCreateOrderInput(validForm())).toEqual({
      customerName: 'Maria Silva',
      customerPhone: '11999998888',
      deliveryAddress: {
        street: 'Av. Paulista',
        number: '1000',
        complement: null,
        neighborhood: 'Bela Vista',
        city: 'São Paulo',
        zipCode: '01310-100',
      },
      items: [
        { name: 'Pizza', quantity: 2, unitPrice: 45 },
        { name: 'Refrigerante', quantity: 1, unitPrice: 9.9 },
      ],
    })
  })
})
