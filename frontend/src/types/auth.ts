export interface AuthUser {
  userId: number
  email: string
  name: string
}

export interface SignupRequest {
  email: string
  password: string
  name: string
}

export interface LoginRequest {
  email: string
  password: string
}

