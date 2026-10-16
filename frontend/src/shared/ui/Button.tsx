import type { ButtonHTMLAttributes } from 'react'

import { cn } from '../lib/cn'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'

export const buttonStyles: Record<Variant, string> = {
  primary: 'bg-ticket text-ink hover:bg-ticket-strong',
  secondary: 'bg-paper text-ink ring-1 ring-inset ring-ink/20 hover:ring-ink/40',
  ghost: 'text-ink hover:bg-ink/5',
  danger: 'bg-danger text-paper hover:bg-danger/90',
}

export const buttonBase =
  'inline-flex items-center justify-center gap-2 rounded-md px-4 h-10 text-[15px] font-semibold ' +
  'transition-colors disabled:cursor-not-allowed disabled:opacity-60'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  /** Mostra que a ação está em andamento e impede clique duplo. */
  loading?: boolean
}

export function Button({ variant = 'primary', loading = false, className, children, disabled, ...rest }: ButtonProps) {
  return (
    <button
      type="button"
      className={cn(buttonBase, buttonStyles[variant], className)}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      {...rest}
    >
      {children}
    </button>
  )
}
