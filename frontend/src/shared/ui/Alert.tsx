import type { ReactNode } from 'react'

import { cn } from '../lib/cn'

type Tone = 'error' | 'success' | 'info'

const TONES: Record<Tone, string> = {
  error: 'bg-danger/8 text-danger ring-danger/30',
  success: 'bg-status-delivered/8 text-status-delivered ring-status-delivered/30',
  info: 'bg-status-received/8 text-ink ring-status-received/30',
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
      className={cn('rounded-md px-4 py-3 text-[15px] ring-1 ring-inset', TONES[tone], className)}
    >
      {children}
    </div>
  )
}
