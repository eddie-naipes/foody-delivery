import type { ButtonHTMLAttributes } from 'react'

import { cn } from '../lib/cn'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'

export const buttonStyles: Record<Variant, string> = {
  primary: 'bg-brand text-paper shadow-brand hover:bg-brand-strong',
  secondary: 'bg-paper text-ink ring-1 ring-inset ring-ink/20 hover:ring-brand/50 hover:text-brand',
  ghost: 'text-ink hover:bg-blush hover:text-brand',
  danger: 'bg-danger text-paper hover:bg-danger/90',
}

export const buttonBase =
  'inline-flex items-center justify-center gap-2 rounded-full px-6 h-11 text-[15px] font-bold ' +
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
