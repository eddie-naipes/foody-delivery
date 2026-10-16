import type { OrderStatus } from './types'

/**
 * Fluxo do pedido, espelhado do enum OrderStatus do back-end. Serve para mostrar
 * apenas as ações possíveis; a API continua sendo a autoridade (responde 422).
 *
 * RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE, e cancelamento em qualquer
 * etapa não final.
 */
export const ORDER_STATUSES: readonly OrderStatus[] = [
  'RECEBIDO',
  'EM_PREPARO',
  'SAIU_PARA_ENTREGA',
  'ENTREGUE',
  'CANCELADO',
]

/** Etapas do caminho feliz, na ordem em que acontecem. */
export const DELIVERY_STEPS: readonly OrderStatus[] = ['RECEBIDO', 'EM_PREPARO', 'SAIU_PARA_ENTREGA', 'ENTREGUE']

interface StatusMeta {
  /** Nome do status para quem lê a lista. */
  label: string
  /** Texto do botão que leva o pedido a este status. */
  action: string
  /** Mensagem exibida depois que a ação dá certo. */
  done: string
  /** Classes de cor (texto + fundo suave + ponto). */
  tone: string
  dot: string
}

export const STATUS_META: Record<OrderStatus, StatusMeta> = {
  RECEBIDO: {
    label: 'Recebido',
    action: 'Receber pedido',
    done: 'Pedido recebido',
    tone: 'text-status-received bg-status-received/10',
    dot: 'bg-status-received',
  },
  EM_PREPARO: {
    label: 'Em preparo',
    action: 'Iniciar preparo',
    done: 'Preparo iniciado',
    tone: 'text-status-preparing bg-status-preparing/10',
    dot: 'bg-status-preparing',
  },
  SAIU_PARA_ENTREGA: {
    label: 'Saiu para entrega',
    action: 'Enviar para entrega',
    done: 'Pedido enviado para entrega',
    tone: 'text-status-out bg-status-out/10',
    dot: 'bg-status-out',
  },
  ENTREGUE: {
    label: 'Entregue',
    action: 'Marcar como entregue',
    done: 'Pedido marcado como entregue',
    tone: 'text-status-delivered bg-status-delivered/10',
    dot: 'bg-status-delivered',
  },
  CANCELADO: {
    label: 'Cancelado',
    action: 'Cancelar pedido',
    done: 'Pedido cancelado',
    tone: 'text-status-canceled bg-status-canceled/10',
    dot: 'bg-status-canceled',
  },
}

function nextStep(status: OrderStatus): OrderStatus | undefined {
  const index = DELIVERY_STEPS.indexOf(status)
  return index >= 0 ? DELIVERY_STEPS[index + 1] : undefined
}

export function isFinal(status: OrderStatus): boolean {
  return nextStep(status) === undefined
}

export function canTransition(from: OrderStatus, to: OrderStatus): boolean {
  if (isFinal(from)) return false
  return to === 'CANCELADO' || to === nextStep(from)
}

/** Ações disponíveis para o pedido: avançar primeiro, cancelar por último. */
export function availableTransitions(status: OrderStatus): OrderStatus[] {
  const next = nextStep(status)
  return next ? [next, 'CANCELADO'] : []
}
