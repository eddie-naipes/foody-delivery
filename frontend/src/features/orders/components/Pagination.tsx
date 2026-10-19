import { Button } from '@/shared/ui/Button'

interface PaginationProps {
  /** Página atual, começando em 1 (como o operador lê). */
  page: number
  totalPages: number
  totalItems: number
  onChange: (page: number) => void
}

/** Anterior/próxima com a posição atual. Só aparece quando há mais de uma página. */
export function Pagination({ page, totalPages, totalItems, onChange }: PaginationProps) {
  if (totalPages <= 1) return null

  return (
    <nav aria-label="Paginação" className="flex flex-wrap items-center justify-between gap-4">
      <Button variant="secondary" disabled={page <= 1} onClick={() => onChange(page - 1)}>
        Anterior
      </Button>
      <p className="text-[15px] text-muted tabular-nums" aria-live="polite">
        Página {page} de {totalPages} ({totalItems} pedidos)
      </p>
      <Button variant="secondary" disabled={page >= totalPages} onClick={() => onChange(page + 1)}>
        Próxima
      </Button>
    </nav>
  )
}
