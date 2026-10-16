import { isAxiosError } from 'axios'

import { MESSAGES } from '../messages'

export interface FieldError {
  field: string
  message: string
}

/** Erro já normalizado no formato da API, pronto para a interface exibir. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: FieldError[]

  constructor(status: number, message: string, fieldErrors: FieldError[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

interface ApiErrorBody {
  message: string
  fieldErrors?: FieldError[]
}

function isApiErrorBody(data: unknown): data is ApiErrorBody {
  return typeof data === 'object' && data !== null && typeof (data as ApiErrorBody).message === 'string'
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  if (isAxiosError(error)) {
    if (!error.response) return new ApiError(0, MESSAGES.networkError)
    const { status, data } = error.response
    if (isApiErrorBody(data)) return new ApiError(status, data.message, data.fieldErrors ?? [])
    return new ApiError(status, MESSAGES.unexpectedError)
  }
  return new ApiError(0, MESSAGES.unexpectedError)
}

/** "items[0].quantity" (API) → "items.0.quantity" (React Hook Form). */
export function toFormFieldPath(apiField: string): string {
  return apiField.replace(/\[(\d+)\]/g, '.$1')
}
