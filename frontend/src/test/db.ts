import type { Order, OrderStatus } from '@/features/orders/types'

export interface TestUser {
  id: number
  name: string
  email: string
  password: string
}

/** Estado da API simulada pelo MSW. Zerado após cada teste. */
export const db = {
  users: [] as TestUser[],
  orders: [] as Order[],
  sequence: { user: 1, order: 1, item: 1 },
}

export function resetDb() {
  db.users = []
  db.orders = []
  db.sequence = { user: 1, order: 1, item: 1 }
}

export function tokenFor(user: TestUser) {
  return `token-${user.id}`
}

export function seedUser(overrides: Partial<TestUser> = {}): TestUser {
  const user = {
    id: db.sequence.user++,
    name: 'Operador',
    email: 'op@foody.com',
    password: 'secret123',
    ...overrides,
  }
  db.users.push(user)
  return user
}

export function seedOrder(
  createdBy: TestUser,
  overrides: Partial<Pick<Order, 'customerName' | 'status' | 'createdAt'>> = {},
): Order {
  const now = new Date().toISOString()
  const order: Order = {
    id: db.sequence.order++,
    customerName: 'Maria Silva',
    customerPhone: '11999998888',
    deliveryAddress: {
      street: 'Av. Paulista',
      number: '1000',
      complement: 'Apto 12',
      neighborhood: 'Bela Vista',
      city: 'São Paulo',
      zipCode: '01310100',
    },
    items: [
      { id: db.sequence.item++, name: 'Pizza Margherita', quantity: 2, unitPrice: 45, subtotal: 90 },
      { id: db.sequence.item++, name: 'Refrigerante', quantity: 1, unitPrice: 9.9, subtotal: 9.9 },
    ],
    totalAmount: 99.9,
    status: 'RECEBIDO' as OrderStatus,
    createdBy: { id: createdBy.id, name: createdBy.name },
    statusHistory: [],
    createdAt: now,
    updatedAt: now,
    ...overrides,
  }
  // Mesma regra do back: nasce RECEBIDO por quem criou; um status inicial diferente vira a etapa seguinte
  const author = { id: createdBy.id, name: createdBy.name }
  order.statusHistory = [{ status: 'RECEBIDO', changedBy: author, changedAt: order.createdAt }]
  if (order.status !== 'RECEBIDO') {
    order.statusHistory.push({ status: order.status, changedBy: author, changedAt: order.updatedAt })
  }
  db.orders.push(order)
  return order
}
