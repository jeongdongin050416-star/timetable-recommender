import type { ReactNode } from 'react'
import { useAuth } from './AuthProvider'

export function ProtectedRoute({ children, fallback }: { children: ReactNode; fallback: ReactNode }) {
  const { user, isLoading } = useAuth()
  if (isLoading) return <main className="auth-page"><p>로그인 상태를 확인하고 있습니다.</p></main>
  return user ? children : fallback
}
