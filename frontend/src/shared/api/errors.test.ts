import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it } from 'vitest'

import { ApiError, toApiError, toFormFieldPath } from './errors'
import { MESSAGES } from '../messages'

function axiosErrorWith(status: number, data: unknown) {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('falhou', 'ERR_BAD_REQUEST', config, {}, {
    status,
    statusText: '',
    headers: {},
    config,
    data,
  })
}

describe('toApiError', () => {
  it('usa o corpo padrão de erro da API', () => {
    const error = toApiError(
      axiosErrorWith(400, {
        status: 400,
        message: 'Dados inválidos',
        fieldErrors: [{ field: 'items[0].quantity', message: 'Quantidade deve ser no mínimo 1' }],
      }),
    )

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(400)
    expect(error.message).toBe('Dados inválidos')
    expect(error.fieldErrors).toEqual([{ field: 'items[0].quantity', message: 'Quantidade deve ser no mínimo 1' }])
  })

  it('explica falha de conexão quando não há resposta', () => {
    const error = toApiError(new AxiosError('Network Error', 'ERR_NETWORK'))

    expect(error.status).toBe(0)
    expect(error.message).toBe(MESSAGES.networkError)
  })

  it('usa mensagem genérica quando a resposta não segue o padrão', () => {
    expect(toApiError(axiosErrorWith(502, '<html>Bad Gateway</html>')).message).toBe(MESSAGES.unexpectedError)
  })

  it('mantém um ApiError como está', () => {
    const original = new ApiError(404, 'Pedido 7 não encontrado')
    expect(toApiError(original)).toBe(original)
  })

  it('trata qualquer outro erro como inesperado', () => {
    expect(toApiError(new Error('boom')).message).toBe(MESSAGES.unexpectedError)
  })
})

describe('toFormFieldPath', () => {
  it('converte índices de lista da API para o formato do formulário', () => {
    expect(toFormFieldPath('items[0].quantity')).toBe('items.0.quantity')
    expect(toFormFieldPath('items[12]')).toBe('items.12')
    expect(toFormFieldPath('deliveryAddress.zipCode')).toBe('deliveryAddress.zipCode')
  })
})
