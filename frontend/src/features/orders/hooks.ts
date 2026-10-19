import { keepPreviousData, useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'

import type { ApiError } from '@/shared/api/errors'

import { ordersApi } from './api'
import type { CreateOrderInput, Order, OrderListParams, OrderStatus, Page, StatusCounts } from './types'

/** Chaves do cache em um só lugar, para invalidar sempre o conjunto certo. */
export const orderKeys = {
  all: ['orders'] as const,
  lists: () => [...orderKeys.all, 'list'] as const,
  list: (params: OrderListParams) => [...orderKeys.lists(), params] as const,
  counts: () => [...orderKeys.all, 'counts'] as const,
  detail: (id: string) => [...orderKeys.all, 'detail', id] as const,
}

/** A expedição acompanha a fila: lista e contagens se atualizam sozinhas a cada 15 segundos. */
const LIST_REFRESH_MS = 15_000

export function useOrders(params: OrderListParams) {
  return useQuery<Page<Order>, ApiError>({
    queryKey: orderKeys.list(params),
    queryFn: () => ordersApi.list(params),
    refetchInterval: LIST_REFRESH_MS,
    // Ao trocar de página ou filtro, a página anterior fica na tela até a nova chegar (sem piscar)
    placeholderData: keepPreviousData,
  })
}

export function useStatusCounts() {
  return useQuery<StatusCounts, ApiError>({
    queryKey: orderKeys.counts(),
    queryFn: ordersApi.statusCount,
    refetchInterval: LIST_REFRESH_MS,
  })
}

export function useOrder(id: string) {
  return useQuery<Order, ApiError>({ queryKey: orderKeys.detail(id), queryFn: () => ordersApi.get(id) })
}

/** Depois de criar ou mudar um pedido, a lista e as contagens deixam de valer. */
function refreshListings(queryClient: QueryClient) {
  void queryClient.invalidateQueries({ queryKey: orderKeys.lists() })
  void queryClient.invalidateQueries({ queryKey: orderKeys.counts() })
}

export function useCreateOrder() {
  const queryClient = useQueryClient()
  return useMutation<Order, ApiError, CreateOrderInput>({
    mutationFn: ordersApi.create,
    onSuccess: (order) => {
      queryClient.setQueryData(orderKeys.detail(String(order.id)), order)
      refreshListings(queryClient)
    },
  })
}

export function useUpdateOrderStatus(id: number) {
  const queryClient = useQueryClient()
  return useMutation<Order, ApiError, OrderStatus>({
    mutationFn: (status) => ordersApi.updateStatus(id, status),
    onSuccess: (order) => {
      queryClient.setQueryData(orderKeys.detail(String(order.id)), order)
      refreshListings(queryClient)
    },
    // Recusa costuma significar que a tela está desatualizada (outra pessoa mudou o pedido): busca o estado real
    onError: () => queryClient.invalidateQueries({ queryKey: orderKeys.all }),
  })
}
