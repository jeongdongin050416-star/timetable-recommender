import { useState, type FormEvent } from 'react'
import { ApiError } from '../api'
import { useAuth } from './AuthProvider'

export function AuthPage() {
  const { login, signup } = useAuth()
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setPending(true)
    setError(null)
    try {
      const email = String(data.get('email') ?? '')
      const password = String(data.get('password') ?? '')
      if (mode === 'login') await login({ email, password })
      else await signup({ email, password, name: String(data.get('name') ?? '') })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : '요청을 처리하지 못했습니다.')
    } finally {
      setPending(false)
    }
  }

  return <main className="auth-page">
    <form className="auth-card" onSubmit={submit}>
      <span className="app-header__brand">KAIST COURSE PLANNER</span>
      <h1>{mode === 'login' ? '로그인' : '회원가입'}</h1>
      <p>나만의 이수 로드맵과 추천 시간표를 관리하세요.</p>
      {mode === 'signup' && <label>이름<input name="name" required maxLength={100} autoComplete="name" /></label>}
      <label>이메일<input name="email" type="email" required autoComplete="email" /></label>
      <label>비밀번호<input name="password" type="password" required minLength={8} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} /></label>
      {error && <div className="app-error" role="alert">{error}</div>}
      <button className="auth-card__submit" disabled={pending}>{pending ? '처리 중…' : mode === 'login' ? '로그인' : '계정 만들기'}</button>
      <button type="button" className="auth-card__switch" onClick={() => { setMode(mode === 'login' ? 'signup' : 'login'); setError(null) }}>
        {mode === 'login' ? '처음이신가요? 회원가입' : '이미 계정이 있나요? 로그인'}
      </button>
    </form>
  </main>
}
