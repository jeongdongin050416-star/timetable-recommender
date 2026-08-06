import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiError, coursesApi } from './api'
import {
  CompletedCoursesPage,
  useCompletedCourses,
} from './features/completedCourses'
import { TimetablePage } from './features/timetable'
import type { CourseSummary, Timetable } from './types'
import { AuthPage } from './auth/AuthPage'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { useAuth } from './auth/AuthProvider'

type AppTab = 'recommendation' | 'roadmap'

function getCoursesErrorMessage(error: unknown) {
  if (!(error instanceof ApiError)) {
    return '전체 과목 목록을 불러오는 중 알 수 없는 오류가 발생했습니다.'
  }
  if (error.code === 'NETWORK_ERROR') {
    return `백엔드 서버에 연결할 수 없습니다. ${error.message}`
  }
  return `전체 과목 조회에 실패했습니다. ${error.message} (${error.code})`
}

function Planner() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState<AppTab>('roadmap')
  const [courses, setCourses] = useState<CourseSummary[]>([])
  const [coursesLoading, setCoursesLoading] = useState(true)
  const [coursesError, setCoursesError] = useState<string | null>(null)
  const [recommendedTimetable, setRecommendedTimetable] = useState<Timetable | null>(null)
  const [isRecommendationStale, setIsRecommendationStale] = useState(false)

  const markRecommendationStale = useCallback(() => {
    setIsRecommendationStale((current) => current || recommendedTimetable !== null)
  }, [recommendedTimetable])
  const completedCourses = useCompletedCourses(markRecommendationStale)

  useEffect(() => {
    const controller = new AbortController()
    setCoursesLoading(true)
    setCoursesError(null)

    coursesApi.getAll(controller.signal)
      .then(({ courses: loadedCourses }) => {
        if (!controller.signal.aborted) setCourses(loadedCourses)
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) setCoursesError(getCoursesErrorMessage(error))
      })
      .finally(() => {
        if (!controller.signal.aborted) setCoursesLoading(false)
      })

    return () => controller.abort()
  }, [])

  const recommendedCourseCodes = useMemo(
    () => new Set(
      recommendedTimetable?.courses.map(({ courseCode }) => courseCode) ?? [],
    ),
    [recommendedTimetable],
  )

  const updateRecommendation = useCallback((timetable: Timetable | null) => {
    setRecommendedTimetable(timetable)
    setIsRecommendationStale(false)
  }, [])

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <span className="app-header__brand">KAIST COURSE PLANNER</span>
          <strong>전산학부 학업 설계</strong>
        </div>
        <div className="app-header__account">
          <span className="app-header__user">{user?.name}</span>
          <button type="button" onClick={() => void logout()}>로그아웃</button>
        </div>
      </header>

      <nav className="app-tabs" aria-label="주요 화면">
        <button
          type="button"
          aria-current={activeTab === 'roadmap' ? 'page' : undefined}
          onClick={() => setActiveTab('roadmap')}
        >
          이수 과목 로드맵
        </button>
        <button
          type="button"
          aria-current={activeTab === 'recommendation' ? 'page' : undefined}
          onClick={() => setActiveTab('recommendation')}
        >
          추천 시간표
          {isRecommendationStale && <i aria-label="추천 결과 갱신 필요" />}
        </button>
      </nav>

      <div hidden={activeTab !== 'roadmap'}>
        <CompletedCoursesPage
          courses={courses}
          coursesLoading={coursesLoading}
          coursesError={coursesError}
          completedCourses={completedCourses}
          recommendedCourseCodes={recommendedCourseCodes}
        />
      </div>

      <div hidden={activeTab !== 'recommendation'}>
        <TimetablePage
          isRecommendationStale={isRecommendationStale}
          onRecommendationChange={updateRecommendation}
        />
      </div>
    </div>
  )
}

function App() {
  return <ProtectedRoute fallback={<AuthPage />}><Planner /></ProtectedRoute>
}

export default App
