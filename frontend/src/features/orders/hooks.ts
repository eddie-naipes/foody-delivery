import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { ApiError } from '@/shared/api/errors'

import { ordersApi } from './api'
import type { CreateOrderInput, Order, OrderStatus } from './types'

/** Chaves do cache em um só lugar, para invalidar sempre o conjunto certo. */
export const orderKeys = {
  all: ['orders'] as const,
  list: () => [...orderKeys.all, 'list'] as const,
  detail: (id: string) => [...orderKeys.all, 'detail', id] as const,
}

/** A expedição acompanha a fila: a lista se atualiza sozinha a cada 15 segundos. */
const LIST_REFRESH_MS = 15_000

export function useOrders() {
  return useQuery<Order[], ApiError>({
    queryKey: orderKeys.list(),
    queryFn: ordersApi.list,
    refetchInterval: LIST_REFRESH_MS,
  })
}

export function useOrder(id: string) {
  return useQuery<Order, ApiError>({ queryKey: orderKeys.detail(id), queryFn: () => ordersApi.get(id) })
}

export function useCreateOrder() {
  const queryClient = useQueryClient()
  return useMutation<Order, ApiError, CreateOrderInput>({
    mutationFn: ordersApi.create,
    onSuccess: (order) => {
      queryClient.setQueryData(orderKeys.detail(String(order.id)), order)
      void queryClient.invalidateQueries({ queryKey: orderKeys.list() })
    },
  })
}

export function useUpdateOrderStatus(id: number) {
  const queryClient = useQueryClient()
  return useMutation<Order, ApiError, OrderStatus>({
    mutationFn: (status) => ordersApi.updateStatus(id, status),
    onSuccess: (order) => {
      queryClient.setQueryData(orderKeys.detail(String(order.id)), order)
      void queryClient.invalidateQueries({ queryKey: orderKeys.list() })
    },
    // Recusa costuma significar que a tela está desatualizada (outra pessoa mudou o pedido): busca o estado real
    onError: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}
