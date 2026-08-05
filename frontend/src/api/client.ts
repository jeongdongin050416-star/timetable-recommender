import type { ApiResponse } from '../types'
import { ApiError } from './ApiError'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
}

function isApiResponse<T>(value: unknown): value is ApiResponse<T> {
  if (typeof value !== 'object' || value === null || !('success' in value)) {
    return false
  }

  const response = value as Record<string, unknown>
  const error = response.error
  const hasValidError =
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    typeof error.code === 'string' &&
    'message' in error &&
    typeof error.message === 'string' &&
    'fieldErrors' in error

  return (
    (response.success === true && 'data' in response && response.error === null) ||
    (response.success === false && response.data === null && hasValidError)
  )
}

export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    })
  } catch (cause) {
    throw new ApiError('서버에 연결할 수 없습니다.', {
      code: 'NETWORK_ERROR',
      cause,
    })
  }

  let rawPayload: unknown
  try {
    rawPayload = await response.json()
  } catch (cause) {
    throw new ApiError('서버 응답을 처리할 수 없습니다.', {
      status: response.status,
      code: 'INVALID_RESPONSE',
      cause,
    })
  }

  if (!isApiResponse<T>(rawPayload)) {
    throw new ApiError('서버 응답 형식이 올바르지 않습니다.', {
      status: response.status,
      code: 'INVALID_RESPONSE',
    })
  }

  const payload = rawPayload

  if (!response.ok || !payload.success) {
    const error = payload.success ? null : payload.error
    throw new ApiError(error?.message ?? `HTTP 오류가 발생했습니다. (${response.status})`, {
      status: response.status,
      code: error?.code ?? 'HTTP_ERROR',
      fieldErrors: error?.fieldErrors,
    })
  }

  return payload.data
}
