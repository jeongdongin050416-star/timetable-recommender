import type { AuthUser, LoginRequest, SignupRequest } from '../types'
import { apiRequest } from './client'

export const authApi = {
  signup: (request: SignupRequest) =>
    apiRequest<AuthUser>('/api/auth/signup', {
      method: 'POST',
      body: request,
    }),

  login: (request: LoginRequest) =>
    apiRequest<AuthUser>('/api/auth/login', {
      method: 'POST',
      body: request,
    }),
}

