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

export interface Order {
  id: number
  customerName: string
  customerPhone: string
  deliveryAddress: Address
  items: OrderItem[]
  totalAmount: number
  status: OrderStatus
  createdBy: { id: number; name: string }
  createdAt: string
  updatedAt: string
}

export interface CreateOrderInput {
  customerName: string
  customerPhone: string
  deliveryAddress: Address
  items: Array<{ name: string; quantity: number; unitPrice: number }>
}
