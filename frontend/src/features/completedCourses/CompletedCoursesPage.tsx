import type { CourseSummary } from '../../types'
import { RoadmapView } from '../roadmap'
import type { UseCompletedCoursesResult } from './useCompletedCourses'

interface CompletedCoursesPageProps {
  courses: readonly CourseSummary[]
  coursesLoading: boolean
  coursesError: string | null
  completedCourses: UseCompletedCoursesResult
  recommendedCourseCodes: ReadonlySet<string>
}

export function CompletedCoursesPage({
  courses,
  coursesLoading,
  coursesError,
  completedCourses,
  recommendedCourseCodes,
}: CompletedCoursesPageProps) {
  return (
    <main className="completed-courses-page">
      {coursesLoading && (
        <p className="app-status" role="status">전체 과목 목록을 불러오는 중입니다.</p>
      )}
      {completedCourses.isLoading && (
        <p className="app-status" role="status">이수 과목을 불러오는 중입니다.</p>
      )}
      {coursesError && <div className="app-error" role="alert">{coursesError}</div>}
      {completedCourses.error && <div className="app-error" role="alert">{completedCourses.error}</div>}
      {completedCourses.toggleError && <div className="app-error" role="alert">{completedCourses.toggleError}</div>}
      {!coursesLoading && !coursesError && courses.length === 0 ? (
        <div className="app-empty" role="status">
          <strong>표시할 과목이 없습니다.</strong>
          <span>백엔드의 과목 데이터 import 상태를 확인해 주세요.</span>
        </div>
      ) : (
        <RoadmapView
          courses={courses}
          completedCourseCodes={completedCourses.completedCourseCodes}
          pendingCourseCodes={completedCourses.pendingCourseCodes}
          recommendedCourseCodes={recommendedCourseCodes}
          isLoading={
            coursesLoading ||
            completedCourses.isLoading ||
            Boolean(coursesError || completedCourses.error)
          }
          onToggleCourse={(courseCode) => void completedCourses.toggleCourse(courseCode)}
        />
      )}
    </main>
  )
}
