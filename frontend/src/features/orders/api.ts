import { http } from '@/shared/api/http'

import type { CreateOrderInput, Order, OrderStatus } from './types'

export const ordersApi = {
  list: () => http.get<Order[]>('/orders').then((response) => response.data),
  get: (id: string) => http.get<Order>(`/orders/${encodeURIComponent(id)}`).then((response) => response.data),
  create: (input: CreateOrderInput) => http.post<Order>('/orders', input).then((response) => response.data),
  updateStatus: (id: number, status: OrderStatus) =>
    http.patch<Order>(`/orders/${id}/status`, { status }).then((response) => response.data),
}
