import { useEffect, useId, useMemo, useState } from 'react'
import { ApiError, completedCoursesApi } from '../../api'
import type {
  CourseSummary,
  RoadmapEdge,
  RoadmapLayout,
  RoadmapNode,
} from '../../types'
import layoutData from './roadmap-layout.json'
import './RoadmapView.css'

const defaultLayout = layoutData as RoadmapLayout
const CANVAS_PADDING = 32

export interface RoadmapViewProps {
  userId: number
  courses: readonly CourseSummary[]
  layout?: RoadmapLayout
}

interface Point {
  x: number
  y: number
}

function getErrorMessage(error: unknown) {
  return error instanceof ApiError
    ? error.message
    : '요청을 처리하는 중 오류가 발생했습니다.'
}

function getNodeCenter(node: RoadmapNode): Point {
  return {
    x: node.x + node.width / 2,
    y: node.y + node.height / 2,
  }
}

function getBoundaryPoint(node: RoadmapNode, toward: Point): Point {
  const center = getNodeCenter(node)
  const dx = toward.x - center.x
  const dy = toward.y - center.y

  if (dx === 0 && dy === 0) {
    return center
  }

  const horizontalScale = dx === 0 ? Number.POSITIVE_INFINITY : node.width / 2 / Math.abs(dx)
  const verticalScale = dy === 0 ? Number.POSITIVE_INFINITY : node.height / 2 / Math.abs(dy)
  const scale = Math.min(horizontalScale, verticalScale)

  return {
    x: center.x + dx * scale,
    y: center.y + dy * scale,
  }
}

function getEdgePath(edge: RoadmapEdge, nodeByCode: Map<string, RoadmapNode>) {
  const fromNode = nodeByCode.get(edge.from)
  const toNode = nodeByCode.get(edge.to)
  if (!fromNode || !toNode) {
    return null
  }

  const fromCenter = getNodeCenter(fromNode)
  const toCenter = getNodeCenter(toNode)
  const start = getBoundaryPoint(fromNode, toCenter)
  const end = getBoundaryPoint(toNode, fromCenter)
  const isMostlyHorizontal = Math.abs(end.x - start.x) >= Math.abs(end.y - start.y)

  if (isMostlyHorizontal) {
    const middleX = (start.x + end.x) / 2
    return `M ${start.x} ${start.y} C ${middleX} ${start.y}, ${middleX} ${end.y}, ${end.x} ${end.y}`
  }

  const middleY = (start.y + end.y) / 2
  return `M ${start.x} ${start.y} C ${start.x} ${middleY}, ${end.x} ${middleY}, ${end.x} ${end.y}`
}

export function RoadmapView({
  userId,
  courses,
  layout = defaultLayout,
}: RoadmapViewProps) {
  const markerPrefix = `roadmap-${useId().replaceAll(':', '')}`
  const [completedCourseCodes, setCompletedCourseCodes] = useState<Set<string>>(
    () => new Set(),
  )
  const [pendingCourseCodes, setPendingCourseCodes] = useState<Set<string>>(
    () => new Set(),
  )
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    let isCurrent = true

    setIsLoading(true)
    setErrorMessage(null)
    completedCoursesApi
      .getAll(userId)
      .then(({ courses: completedCourses }) => {
        if (isCurrent) {
          setCompletedCourseCodes(
            new Set(completedCourses.map(({ courseCode }) => courseCode)),
          )
        }
      })
      .catch((error: unknown) => {
        if (isCurrent) {
          setErrorMessage(getErrorMessage(error))
        }
      })
      .finally(() => {
        if (isCurrent) {
          setIsLoading(false)
        }
      })

    return () => {
      isCurrent = false
    }
  }, [userId])

  const courseByCode = useMemo(
    () => new Map(courses.map((course) => [course.courseCode, course])),
    [courses],
  )
  const nodeByCode = useMemo(
    () => new Map(layout.nodes.map((node) => [node.courseCode, node])),
    [layout.nodes],
  )
  const canvasSize = useMemo(
    () => ({
      width:
        Math.max(0, ...layout.nodes.map((node) => node.x + node.width)) +
        CANVAS_PADDING,
      height:
        Math.max(0, ...layout.nodes.map((node) => node.y + node.height)) +
        CANVAS_PADDING,
    }),
    [layout.nodes],
  )

  const toggleCourse = async (courseCode: string) => {
    if (isLoading || pendingCourseCodes.has(courseCode)) {
      return
    }

    const wasCompleted = completedCourseCodes.has(courseCode)
    setErrorMessage(null)
    setCompletedCourseCodes((current) => {
      const next = new Set(current)
      if (wasCompleted) {
        next.delete(courseCode)
      } else {
        next.add(courseCode)
      }
      return next
    })
    setPendingCourseCodes((current) => new Set(current).add(courseCode))

    try {
      if (wasCompleted) {
        await completedCoursesApi.remove(userId, courseCode)
      } else {
        await completedCoursesApi.add(userId, courseCode)
      }
    } catch (error) {
      setCompletedCourseCodes((current) => {
        const rolledBack = new Set(current)
        if (wasCompleted) {
          rolledBack.add(courseCode)
        } else {
          rolledBack.delete(courseCode)
        }
        return rolledBack
      })
      setErrorMessage(getErrorMessage(error))
    } finally {
      setPendingCourseCodes((current) => {
        const next = new Set(current)
        next.delete(courseCode)
        return next
      })
    }
  }

  return (
    <section className="roadmap-view" aria-labelledby="roadmap-title">
      <header className="roadmap-view__header">
        <div>
          <p className="roadmap-view__eyebrow">COURSE ROADMAP</p>
          <h2 id="roadmap-title">전산학부 과목 로드맵</h2>
          <p>과목 카드를 선택해 이수 여부를 변경할 수 있습니다.</p>
        </div>
        <div className="roadmap-view__legend" aria-label="과목 관계 범례">
          <span><i className="roadmap-view__line" /> 선수 과목</span>
          <span><i className="roadmap-view__line roadmap-view__line--dashed" /> 권장 과목</span>
        </div>
      </header>

      {errorMessage && (
        <div className="roadmap-view__error" role="alert">
          {errorMessage}
        </div>
      )}
      {isLoading && (
        <p className="roadmap-view__loading" role="status">
          이수 정보를 불러오는 중입니다.
        </p>
      )}

      <div className="roadmap-view__scroller" tabIndex={0} aria-label="과목 로드맵">
        <div
          className="roadmap-view__canvas"
          style={{ width: canvasSize.width, height: canvasSize.height }}
        >
          <svg
            className="roadmap-view__edges"
            width={canvasSize.width}
            height={canvasSize.height}
            viewBox={`0 0 ${canvasSize.width} ${canvasSize.height}`}
            aria-hidden="true"
          >
            <defs>
              <marker
                id={`${markerPrefix}-prerequisite`}
                viewBox="0 0 10 10"
                refX="9"
                refY="5"
                markerWidth="7"
                markerHeight="7"
                orient="auto-start-reverse"
              >
                <path d="M 0 0 L 10 5 L 0 10 z" className="roadmap-edge-marker" />
              </marker>
              <marker
                id={`${markerPrefix}-recommended`}
                viewBox="0 0 10 10"
                refX="9"
                refY="5"
                markerWidth="7"
                markerHeight="7"
                orient="auto-start-reverse"
              >
                <path d="M 0 0 L 10 5 L 0 10 z" className="roadmap-edge-marker roadmap-edge-marker--recommended" />
              </marker>
            </defs>
            {layout.edges.map((edge, index) => {
              const path = getEdgePath(edge, nodeByCode)
              if (!path) {
                return null
              }

              const isRecommended = edge.relationType === 'RECOMMENDED'
              return (
                <path
                  key={`${edge.from}-${edge.to}-${index}`}
                  d={path}
                  className={`roadmap-edge${isRecommended ? ' roadmap-edge--recommended' : ''}`}
                  markerEnd={`url(#${markerPrefix}-${isRecommended ? 'recommended' : 'prerequisite'})`}
                />
              )
            })}
          </svg>

          <div className="roadmap-view__nodes">
            {layout.nodes.map((node) => {
              const course = courseByCode.get(node.courseCode)
              const isCompleted = completedCourseCodes.has(node.courseCode)
              const isPending = pendingCourseCodes.has(node.courseCode)

              return (
                <button
                  key={node.courseCode}
                  type="button"
                  className="roadmap-node"
                  data-completed={isCompleted}
                  style={{
                    left: node.x,
                    top: node.y,
                    width: node.width,
                    height: node.height,
                  }}
                  aria-pressed={isCompleted}
                  aria-label={`${course?.name ?? node.courseCode}, ${isCompleted ? '이수함' : '이수하지 않음'}`}
                  disabled={isLoading || isPending || !course}
                  onClick={() => void toggleCourse(node.courseCode)}
                >
                  <span className="roadmap-node__topline">
                    <strong>{node.courseCode}</strong>
                    <span className="roadmap-node__check" aria-hidden="true">
                      {isCompleted ? '✓' : ''}
                    </span>
                  </span>
                  <span className="roadmap-node__name">
                    {course?.name ?? '과목 정보 없음'}
                  </span>
                  <span className="roadmap-node__meta">
                    {isPending ? '저장 중…' : course ? `${course.credits}학점` : '선택 불가'}
                  </span>
                </button>
              )
            })}
          </div>
        </div>
      </div>
    </section>
  )
}

