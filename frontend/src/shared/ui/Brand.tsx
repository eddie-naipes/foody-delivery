import { cn } from '../lib/cn'

/** Marca da aplicação: ícone + "Foody Delivery" com o "Delivery" em coral, como na identidade da empresa. */
export function Brand({ className, inverted = false }: { className?: string; inverted?: boolean }) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <span
        aria-hidden="true"
        className={cn(
          'grid size-9 place-items-center rounded-[11px] text-[18px] font-extrabold',
          inverted ? 'bg-paper text-brand' : 'bg-brand text-paper',
        )}
      >
        F
      </span>
      <span className="text-[18px] leading-none font-extrabold tracking-tight">
        <span className={inverted ? 'text-paper' : 'text-ink'}>Foody</span>{' '}
        <span className={inverted ? 'text-paper/85' : 'text-coral-ink'}>Delivery</span>
      </span>
    </span>
  )
}
