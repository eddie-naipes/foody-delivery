import type { UseFormRegisterReturn } from 'react-hook-form'

/**
 * Aplica uma máscara a um campo registrado no React Hook Form: o valor é corrigido
 * antes de o formulário lê-lo, então o estado nunca guarda o texto inválido.
 */
export function masked<T extends string>(
  registration: UseFormRegisterReturn<T>,
  mask: (value: string) => string,
): UseFormRegisterReturn<T> {
  return {
    ...registration,
    onChange: (event) => {
      const input = event.target as HTMLInputElement
      input.value = mask(input.value)
      return registration.onChange(event)
    },
  }
}
