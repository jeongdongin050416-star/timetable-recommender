import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError, completedCoursesApi } from '../../api'

export interface UseCompletedCoursesResult {
  completedCourseCodes: ReadonlySet<string>
  pendingCourseCodes: ReadonlySet<string>
  isLoading: boolean
  error: string | null
  toggleError: string | null
  addCourse: (courseCode: string) => Promise<boolean>
  removeCourse: (courseCode: string) => Promise<boolean>
  toggleCourse: (courseCode: string) => Promise<boolean>
}

function getReadErrorMessage(error: unknown) {
  if (!(error instanceof ApiError)) {
    return '이수 과목을 불러오는 중 알 수 없는 오류가 발생했습니다.'
  }
  if (error.code === 'USER_NOT_FOUND') {
    return `사용자 정보를 찾을 수 없습니다. ${error.message}`
  }
  if (error.code === 'NETWORK_ERROR') {
    return `백엔드 서버에 연결할 수 없습니다. ${error.message}`
  }
  return `이수 과목 조회에 실패했습니다. ${error.message} (${error.code})`
}

function getMutationErrorMessage(error: unknown, isAdding: boolean) {
  const action = isAdding ? '추가' : '삭제'
  if (!(error instanceof ApiError)) {
    return `이수 과목 ${action} 중 알 수 없는 오류가 발생했습니다.`
  }
  if (error.code === 'USER_NOT_FOUND') {
    return `사용자를 찾을 수 없어 이수 상태를 저장하지 못했습니다. ${error.message}`
  }
  if (error.code === 'COURSE_NOT_FOUND') {
    return `서버에 존재하지 않는 과목이어서 저장하지 못했습니다. ${error.message}`
  }
  if (error.code === 'NETWORK_ERROR') {
    return `백엔드 서버에 연결할 수 없어 이수 과목 ${action}에 실패했습니다.`
  }
  return `이수 과목 ${action}에 실패했습니다. ${error.message} (${error.code})`
}

export function useCompletedCourses(
  onCourseChanged?: () => void,
): UseCompletedCoursesResult {
  const [completedCourseCodes, setCompletedCourseCodes] = useState<Set<string>>(() => new Set())
  const [pendingCourseCodes, setPendingCourseCodes] = useState<Set<string>>(() => new Set())
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [toggleError, setToggleError] = useState<string | null>(null)
  const completedRef = useRef<Set<string>>(new Set())
  const pendingRef = useRef<Set<string>>(new Set())
  const mountedRef = useRef(false)

  useEffect(() => {
    mountedRef.current = true
    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    let isCurrent = true
    completedRef.current = new Set()
    pendingRef.current = new Set()
    setCompletedCourseCodes(new Set())
    setPendingCourseCodes(new Set())
    setIsLoading(true)
    setError(null)
    setToggleError(null)

    completedCoursesApi.getAll()
      .then(({ courses }) => {
        if (!isCurrent || !mountedRef.current) return
        const codes = new Set(courses.map(({ courseCode }) => courseCode))
        completedRef.current = codes
        setCompletedCourseCodes(codes)
      })
      .catch((requestError: unknown) => {
        if (isCurrent && mountedRef.current) {
          setError(getReadErrorMessage(requestError))
        }
      })
      .finally(() => {
        if (isCurrent && mountedRef.current) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [])

  const mutateCourse = useCallback(async (courseCode: string, shouldComplete: boolean) => {
    if (isLoading || pendingRef.current.has(courseCode)) return false

    const wasCompleted = completedRef.current.has(courseCode)
    if (wasCompleted === shouldComplete) return true

    const optimisticCompleted = new Set(completedRef.current)
    if (shouldComplete) optimisticCompleted.add(courseCode)
    else optimisticCompleted.delete(courseCode)
    completedRef.current = optimisticCompleted
    pendingRef.current = new Set(pendingRef.current).add(courseCode)

    if (mountedRef.current) {
      setToggleError(null)
      setCompletedCourseCodes(optimisticCompleted)
      setPendingCourseCodes(new Set(pendingRef.current))
    }

    try {
      if (shouldComplete) await completedCoursesApi.add(courseCode)
      else await completedCoursesApi.remove(courseCode)
      if (mountedRef.current) onCourseChanged?.()
      return true
    } catch (requestError) {
      const rolledBack = new Set(completedRef.current)
      if (wasCompleted) rolledBack.add(courseCode)
      else rolledBack.delete(courseCode)
      completedRef.current = rolledBack
      if (mountedRef.current) {
        setCompletedCourseCodes(rolledBack)
        setToggleError(getMutationErrorMessage(requestError, shouldComplete))
      }
      return false
    } finally {
      const nextPending = new Set(pendingRef.current)
      nextPending.delete(courseCode)
      pendingRef.current = nextPending
      if (mountedRef.current) setPendingCourseCodes(nextPending)
    }
  }, [isLoading, onCourseChanged])

  const addCourse = useCallback(
    (courseCode: string) => mutateCourse(courseCode, true),
    [mutateCourse],
  )
  const removeCourse = useCallback(
    (courseCode: string) => mutateCourse(courseCode, false),
    [mutateCourse],
  )
  const toggleCourse = useCallback(
    (courseCode: string) => mutateCourse(courseCode, !completedRef.current.has(courseCode)),
    [mutateCourse],
  )

  return {
    completedCourseCodes,
    pendingCourseCodes,
    isLoading,
    error,
    toggleError,
    addCourse,
    removeCourse,
    toggleCourse,
  }
}
