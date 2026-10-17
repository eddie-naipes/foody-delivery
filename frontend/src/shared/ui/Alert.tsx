import type { ReactNode } from 'react'

import { cn } from '../lib/cn'

type Tone = 'error' | 'success' | 'info'

const TONES: Record<Tone, string> = {
  error: 'bg-blush text-danger ring-danger/25',
  success: 'bg-status-delivered/8 text-status-delivered ring-status-delivered/30',
  info: 'bg-blush text-ink ring-coral/40',
}

interface AlertProps {
  tone: Tone
  children: ReactNode
  className?: string
}

/** Erros interrompem (role="alert"); confirmações e avisos são anunciados com calma (role="status"). */
export function Alert({ tone, children, className }: AlertProps) {
  return (
    <div
      role={tone === 'error' ? 'alert' : 'status'}
      className={cn('rounded-xl px-4 py-3 text-[15px] font-medium ring-1 ring-inset', TONES[tone], className)}
    >
      {children}
    </div>
  )
}
