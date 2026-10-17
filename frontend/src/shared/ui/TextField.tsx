import { useId, type InputHTMLAttributes, type Ref } from 'react'

import { cn } from '../lib/cn'

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  /** Esconde o rótulo visualmente (continua lido por leitores de tela). */
  hideLabel?: boolean
  error?: string
  hint?: string
  ref?: Ref<HTMLInputElement>
}

export function TextField({ label, hideLabel, error, hint, className, ref, ...rest }: TextFieldProps) {
  const id = useId()
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined

  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      <label htmlFor={id} className={cn('text-[14px] font-bold text-ink', hideLabel && 'sr-only')}>
        {label}
      </label>
      <input
        id={id}
        ref={ref}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={cn(
          'h-11 w-full rounded-xl bg-paper px-4 text-[15px] text-ink ring-1 ring-inset placeholder:text-muted/70',
          'transition-shadow focus:outline-none focus:ring-2',
          error ? 'ring-danger focus:ring-danger' : 'ring-ink/20 hover:ring-ink/35 focus:ring-ink',
        )}
        {...rest}
      />
      {error ? (
        <p id={`${id}-error`} className="text-[13px] text-danger">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="text-[13px] text-muted">
          {hint}
        </p>
      ) : null}
    </div>
  )
}
