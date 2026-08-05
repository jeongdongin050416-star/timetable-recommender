import type { FieldErrors } from '../types'

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: FieldErrors | null

  constructor(
    message: string,
    options: {
      status?: number
      code?: string
      fieldErrors?: FieldErrors | null
      cause?: unknown
    } = {},
  ) {
    super(message, { cause: options.cause })
    this.name = 'ApiError'
    this.status = options.status ?? 0
    this.code = options.code ?? 'UNKNOWN_ERROR'
    this.fieldErrors = options.fieldErrors ?? null
  }
}

