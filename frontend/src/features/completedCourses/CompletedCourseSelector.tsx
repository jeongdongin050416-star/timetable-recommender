import { useMemo, useState } from 'react'
import type { CourseSummary } from '../../types'
import './CompletedCourseSelector.css'

export interface CompletedCourseSelectorProps {
  userId: number
  courses: readonly CourseSummary[]
  completedCourseCodes: ReadonlySet<string>
  pendingCourseCodes: ReadonlySet<string>
  isLoading: boolean
  error?: string | null
  onToggleCourse: (courseCode: string) => void
}

export function CompletedCourseSelector({
  userId,
  courses,
  completedCourseCodes,
  pendingCourseCodes,
  isLoading,
  error = null,
  onToggleCourse,
}: CompletedCourseSelectorProps) {
  const [query, setQuery] = useState('')

  const filteredCourses = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase()
    if (!normalizedQuery) {
      return courses
    }

    return courses.filter(
      ({ courseCode, name }) =>
        courseCode.toLocaleLowerCase().includes(normalizedQuery) ||
        name.toLocaleLowerCase().includes(normalizedQuery),
    )
  }, [courses, query])

  return (
    <section className="completed-course-selector" aria-labelledby={`course-selector-title-${userId}`}>
      <div className="completed-course-selector__header">
        <div>
          <p className="completed-course-selector__eyebrow">COMPLETED COURSES</p>
          <h2 id={`course-selector-title-${userId}`}>이수 과목 선택</h2>
          <p>이수한 과목을 선택하면 추천 대상에서 제외됩니다.</p>
        </div>
        <span className="completed-course-selector__count">
          {completedCourseCodes.size}개 선택
        </span>
      </div>

      <label className="completed-course-selector__search">
        <span className="completed-course-selector__search-label">과목 검색</span>
        <input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="과목명 또는 과목 코드"
        />
      </label>

      {error && (
        <div className="completed-course-selector__error" role="alert">
          {error}
        </div>
      )}

      {isLoading ? (
        <p className="completed-course-selector__status">이수 과목을 불러오는 중입니다.</p>
      ) : filteredCourses.length === 0 ? (
        <p className="completed-course-selector__status">
          검색 결과에 해당하는 과목이 없습니다.
        </p>
      ) : (
        <ul className="completed-course-selector__grid">
          {filteredCourses.map((course) => {
            const isCompleted = completedCourseCodes.has(course.courseCode)
            const isPending = pendingCourseCodes.has(course.courseCode)

            return (
              <li key={course.courseCode}>
                <button
                  type="button"
                  className="course-option"
                  data-selected={isCompleted}
                  aria-pressed={isCompleted}
                  aria-label={`${course.name}, ${isCompleted ? '이수함' : '이수하지 않음'}`}
                  disabled={isPending}
                  onClick={() => onToggleCourse(course.courseCode)}
                >
                  <span className="course-option__check" aria-hidden="true">
                    {isCompleted ? '✓' : ''}
                  </span>
                  <span className="course-option__content">
                    <strong>{course.name}</strong>
                    <span>
                      {course.courseCode} · {course.credits}학점
                    </span>
                  </span>
                  {isPending && (
                    <span className="course-option__pending" aria-hidden="true">
                      저장 중
                    </span>
                  )}
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
