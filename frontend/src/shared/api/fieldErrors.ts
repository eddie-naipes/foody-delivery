import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'

import { toFormFieldPath, type ApiError } from './errors'

/**
 * Coloca os fieldErrors devolvidos pela API nos campos do formulário.
 * Retorna false quando não havia erro de campo — aí a mensagem geral deve ser exibida.
 */
export function applyApiFieldErrors<T extends FieldValues>(error: ApiError, setError: UseFormSetError<T>): boolean {
  error.fieldErrors.forEach(({ field, message }, index) => {
    setError(toFormFieldPath(field) as Path<T>, { type: 'server', message }, { shouldFocus: index === 0 })
  })
  return error.fieldErrors.length > 0
}
