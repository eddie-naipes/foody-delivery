/** Contratos da API de pedidos (espelham os DTOs do back-end). */

export type OrderStatus = 'RECEBIDO' | 'EM_PREPARO' | 'SAIU_PARA_ENTREGA' | 'ENTREGUE' | 'CANCELADO'

export interface Address {
  street: string
  number: string
  complement: string | null
  neighborhood: string
  city: string
  zipCode: string
}

export interface OrderItem {
  id: number
  name: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface UserSummary {
  id: number
  name: string
}

/** Uma etapa da linha do tempo. Sem autor em pedidos anteriores ao histórico. */
export interface StatusChange {
  status: OrderStatus
  changedBy: UserSummary | null
  changedAt: string
}

export interface Order {
  id: number
  customerName: string
  customerPhone: string
  deliveryAddress: Address
  items: OrderItem[]
  totalAmount: number
  status: OrderStatus
  createdBy: UserSummary
  /** Da criação ao status atual, em ordem cronológica. */
  statusHistory: StatusChange[]
  createdAt: string
  updatedAt: string
}

export interface CreateOrderInput {
  customerName: string
  customerPhone: string
  deliveryAddress: Address
  items: Array<{ name: string; quantity: number; unitPrice: number }>
}

/** Página de resultados (PageResponse do back-end). page começa em 0. */
export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface StatusCounts {
  total: number
  byStatus: Record<OrderStatus, number>
}

/** O que a lista pede à API: um status opcional e a página (começando em 0). */
export interface OrderListParams {
  status?: OrderStatus
  page: number
}
