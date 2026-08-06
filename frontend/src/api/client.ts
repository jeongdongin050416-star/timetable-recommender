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
      credentials: 'include',
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    })
  } catch (cause) {
    throw new ApiError('서버에 연결할 수 없습니다.', {
      code: 'NETWORK_ERROR',
      cause,
    })
  }

  let responseBody: string
  try {
    responseBody = await response.text()
  } catch (cause) {
    throw new ApiError('서버 응답을 읽을 수 없습니다.', {
      status: response.status,
      code: 'INVALID_RESPONSE',
      cause,
    })
  }

  let rawPayload: unknown
  try {
    rawPayload = JSON.parse(responseBody)
  } catch (cause) {
    if (response.status >= 500 || responseBody.trim() === '') {
      throw new ApiError('백엔드 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해 주세요.', {
        status: response.status,
        code: 'NETWORK_ERROR',
        cause,
      })
    }
    const contentType = response.headers.get('content-type') ?? ''
    const isHtml = contentType.includes('text/html') || /^\s*<!doctype html/i.test(responseBody)
    const isCorsRejection = response.status === 403 && responseBody.includes('Invalid CORS request')
    const message = isCorsRejection
      ? '현재 웹 주소가 CORS 허용 목록에 없습니다. CORS_ALLOWED_ORIGIN 설정을 확인해 주세요.'
      : isHtml
        ? 'API 요청이 백엔드가 아닌 프런트 서버로 전달되었습니다. VITE_API_BASE_URL 설정을 확인해 주세요.'
        : `서버가 JSON이 아닌 응답을 반환했습니다. (HTTP ${response.status})`
    throw new ApiError(message, {
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
