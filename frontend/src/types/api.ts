export type FieldErrors = Record<string, string>

export interface ApiErrorBody {
  code: string
  message: string
  fieldErrors: FieldErrors | null
}

export type ApiResponse<T> =
  | { success: true; data: T; error: null }
  | { success: false; data: null; error: ApiErrorBody }

