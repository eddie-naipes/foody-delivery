import { http } from '@/shared/api/http'

import type { CreateOrderInput, Order, OrderListParams, OrderStatus, Page, StatusCounts } from './types'

/** 12 comandas por página: 4 linhas da grade de 3 colunas. */
export const ORDERS_PAGE_SIZE = 12

export const ordersApi = {
  list: ({ status, page }: OrderListParams) =>
    http
      .get<Page<Order>>('/orders', { params: { status, page, size: ORDERS_PAGE_SIZE } })
      .then((response) => response.data),
  statusCount: () => http.get<StatusCounts>('/orders/status-count').then((response) => response.data),
  get: (id: string) => http.get<Order>(`/orders/${encodeURIComponent(id)}`).then((response) => response.data),
  create: (input: CreateOrderInput) => http.post<Order>('/orders', input).then((response) => response.data),
  updateStatus: (id: number, status: OrderStatus) =>
    http.patch<Order>(`/orders/${id}/status`, { status }).then((response) => response.data),
}
