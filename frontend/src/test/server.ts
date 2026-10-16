import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

import { canTransition } from '@/features/orders/status'
import type { CreateOrderInput, OrderStatus } from '@/features/orders/types'

import { db, tokenFor, type TestUser } from './db'

/** Imita o formato de erro da API real (ApiError do back-end). */
export function apiError(status: number, message: string, fieldErrors: { field: string; message: string }[] = []) {
  return HttpResponse.json(
    { status, error: '', message, fieldErrors, timestamp: new Date().toISOString() },
    { status },
  )
}

function authenticatedUser(request: Request): TestUser | undefined {
  const token = request.headers.get('Authorization')?.replace('Bearer ', '')
  return db.users.find((user) => tokenFor(user) === token)
}

function authResponse(user: TestUser, status = 200) {
  return HttpResponse.json(
    { token: tokenFor(user), user: { id: user.id, name: user.name, email: user.email } },
    { status },
  )
}

const unauthorized = () => apiError(401, 'Autenticação necessária')

export const handlers = [
  http.post('*/api/auth/register', async ({ request }) => {
    const body = (await request.json()) as { name: string; email: string; password: string }
    const email = body.email.trim().toLowerCase()
    if (db.users.some((user) => user.email === email)) {
      return apiError(409, 'E-mail já cadastrado')
    }
    const user = { id: db.sequence.user++, name: body.name.trim(), email, password: body.password }
    db.users.push(user)
    return authResponse(user, 201)
  }),

  http.post('*/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { email: string; password: string }
    const user = db.users.find((u) => u.email === body.email.trim().toLowerCase() && u.password === body.password)
    return user ? authResponse(user) : apiError(401, 'E-mail ou senha inválidos')
  }),

  http.get('*/api/orders', ({ request }) => {
    if (!authenticatedUser(request)) return unauthorized()
    return HttpResponse.json([...db.orders].reverse())
  }),

  http.get('*/api/orders/:id', ({ request, params }) => {
    if (!authenticatedUser(request)) return unauthorized()
    const order = db.orders.find((o) => o.id === Number(params.id))
    return order ? HttpResponse.json(order) : apiError(404, `Pedido ${String(params.id)} não encontrado`)
  }),

  http.post('*/api/orders', async ({ request }) => {
    const user = authenticatedUser(request)
    if (!user) return unauthorized()
    const body = (await request.json()) as CreateOrderInput
    const now = new Date().toISOString()
    const items = body.items.map((item) => ({
      id: db.sequence.item++,
      ...item,
      subtotal: item.quantity * item.unitPrice,
    }))
    const order = {
      id: db.sequence.order++,
      customerName: body.customerName,
      customerPhone: body.customerPhone,
      deliveryAddress: { ...body.deliveryAddress, zipCode: body.deliveryAddress.zipCode.replace('-', '') },
      items,
      totalAmount: items.reduce((sum, item) => sum + item.subtotal, 0),
      status: 'RECEBIDO' as OrderStatus,
      createdBy: { id: user.id, name: user.name },
      createdAt: now,
      updatedAt: now,
    }
    db.orders.push(order)
    return HttpResponse.json(order, { status: 201, headers: { Location: `/api/orders/${order.id}` } })
  }),

  http.patch('*/api/orders/:id/status', async ({ request, params }) => {
    if (!authenticatedUser(request)) return unauthorized()
    const order = db.orders.find((o) => o.id === Number(params.id))
    if (!order) return apiError(404, `Pedido ${String(params.id)} não encontrado`)
    const { status } = (await request.json()) as { status: OrderStatus }
    if (!canTransition(order.status, status)) {
      return apiError(422, `Transição de ${order.status} para ${status} não permitida`)
    }
    order.status = status
    order.updatedAt = new Date().toISOString()
    return HttpResponse.json(order)
  }),
]

export const server = setupServer(...handlers)
