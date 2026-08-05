import { useId, useMemo, useState } from 'react'
import type {
  CourseSummary,
  RoadmapEdge,
  RoadmapLayout,
  RoadmapNode,
} from '../../types'
import layoutData from './roadmap-layout.json'
import './RoadmapView.css'

const defaultLayout: RoadmapLayout = {
  groups: layoutData.groups,
  nodes: layoutData.nodes,
  edges: layoutData.edges.map((edge) => ({
    ...edge,
    relationType: edge.relationType === 'PREREQUISITE'
      ? 'PREREQUISITE'
      : 'RECOMMENDED',
  })),
}
const CANVAS_PADDING = 32

export interface RoadmapViewProps {
  courses: readonly CourseSummary[]
  completedCourseCodes: ReadonlySet<string>
  pendingCourseCodes: ReadonlySet<string>
  recommendedCourseCodes?: ReadonlySet<string>
  isLoading?: boolean
  onToggleCourse: (courseCode: string) => void
  layout?: RoadmapLayout
}

interface Point {
  x: number
  y: number
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
  courses,
  completedCourseCodes,
  pendingCourseCodes,
  recommendedCourseCodes = new Set<string>(),
  isLoading = false,
  onToggleCourse,
  layout = defaultLayout,
}: RoadmapViewProps) {
  const markerPrefix = `roadmap-${useId().replaceAll(':', '')}`
  const [query, setQuery] = useState('')
  const normalizedQuery = query.trim().toLocaleLowerCase()

  const courseByCode = useMemo(
    () => new Map(courses.map((course) => [course.courseCode, course])),
    [courses],
  )
  const nodeByCode = useMemo(
    () => new Map(layout.nodes.map((node) => [node.courseCode, node])),
    [layout.nodes],
  )
  const completedCourses = useMemo(
    () => courses.filter((course) => completedCourseCodes.has(course.courseCode)),
    [completedCourseCodes, courses],
  )
  const completedCredits = useMemo(
    () => completedCourses.reduce((total, course) => total + course.credits, 0),
    [completedCourses],
  )
  const matchCount = useMemo(
    () => normalizedQuery
      ? courses.filter(({ courseCode, name }) =>
          courseCode.toLocaleLowerCase().includes(normalizedQuery) ||
          name.toLocaleLowerCase().includes(normalizedQuery),
        ).length
      : courses.length,
    [courses, normalizedQuery],
  )
  const canvasSize = useMemo(
    () => ({
      width: Math.max(0, ...layout.nodes.map((node) => node.x + node.width)) + CANVAS_PADDING,
      height: Math.max(0, ...layout.nodes.map((node) => node.y + node.height)) + CANVAS_PADDING,
    }),
    [layout.nodes],
  )

  return (
    <section className="roadmap-view" aria-labelledby="roadmap-title">
      <header className="roadmap-view__header">
        <div>
          <p className="roadmap-view__eyebrow">COURSE ROADMAP</p>
          <h2 id="roadmap-title">전산학부 과목 로드맵</h2>
          <p>과목 카드를 선택해 이수 여부를 변경할 수 있습니다.</p>
        </div>
        <div className="roadmap-view__legend" aria-label="로드맵 범례">
          <span><i className="roadmap-view__swatch roadmap-view__swatch--completed" /> 이수 완료</span>
          <span><i className="roadmap-view__swatch roadmap-view__swatch--recommended" /> 추천 과목</span>
          <span><i className="roadmap-view__line roadmap-view__line--dashed" /> 권장 이수 관계</span>
        </div>
      </header>

      <div className="roadmap-view__toolbar">
        <label className="roadmap-view__search">
          <span>과목 검색</span>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="과목 코드 또는 과목명"
          />
        </label>
        <dl className="roadmap-view__summary" aria-label="이수 현황">
          <div><dt>이수 과목</dt><dd>{completedCourses.length}개</dd></div>
          <div><dt>이수 학점</dt><dd>{completedCredits}학점</dd></div>
          <div><dt>전체 과목</dt><dd>{courses.length}개</dd></div>
        </dl>
      </div>

      {normalizedQuery && (
        <p className="roadmap-view__search-result" role="status">
          검색 결과 {matchCount}개 · 일치하지 않는 과목은 흐리게 표시됩니다.
        </p>
      )}

      <div className="roadmap-view__scroller" tabIndex={0} aria-label="과목 로드맵">
        <div
          className="roadmap-view__canvas"
          style={{ width: canvasSize.width, height: canvasSize.height }}
        >
          <div className="roadmap-view__areas" aria-hidden="true">
            {(layout.groups ?? []).map((group) => (
              <div
                key={group.id}
                className="roadmap-area"
                style={{
                  left: group.x,
                  top: group.y,
                  width: group.width,
                  height: group.height,
                  backgroundColor: group.color,
                }}
              >
                <span>{group.title}</span>
              </div>
            ))}
          </div>

          <svg
            className="roadmap-view__edges"
            width={canvasSize.width}
            height={canvasSize.height}
            viewBox={`0 0 ${canvasSize.width} ${canvasSize.height}`}
            aria-hidden="true"
          >
            <defs>
              <marker id={`${markerPrefix}-prerequisite`} viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
                <path d="M 0 0 L 10 5 L 0 10 z" className="roadmap-edge-marker" />
              </marker>
              <marker id={`${markerPrefix}-recommended`} viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
                <path d="M 0 0 L 10 5 L 0 10 z" className="roadmap-edge-marker roadmap-edge-marker--recommended" />
              </marker>
            </defs>
            {layout.edges.map((edge, index) => {
              const path = getEdgePath(edge, nodeByCode)
              if (!path) return null
              const isRecommendedRelation = edge.relationType === 'RECOMMENDED'
              return (
                <path
                  key={`${edge.from}-${edge.to}-${index}`}
                  d={path}
                  className={`roadmap-edge${isRecommendedRelation ? ' roadmap-edge--recommended' : ''}`}
                  markerEnd={`url(#${markerPrefix}-${isRecommendedRelation ? 'recommended' : 'prerequisite'})`}
                />
              )
            })}
          </svg>

          <div className="roadmap-view__nodes">
            {layout.nodes.map((node) => {
              const course = courseByCode.get(node.courseCode)
              const isCompleted = completedCourseCodes.has(node.courseCode)
              const isPending = pendingCourseCodes.has(node.courseCode)
              const isRecommended = recommendedCourseCodes.has(node.courseCode)
              const isSearchMatch = !normalizedQuery || Boolean(course && (
                course.courseCode.toLocaleLowerCase().includes(normalizedQuery) ||
                course.name.toLocaleLowerCase().includes(normalizedQuery)
              ))

              return (
                <button
                  key={node.courseCode}
                  type="button"
                  className="roadmap-node"
                  data-completed={isCompleted}
                  data-recommended={isRecommended}
                  data-pending={isPending}
                  data-search-match={isSearchMatch}
                  style={{ left: node.x, top: node.y, width: node.width, height: node.height }}
                  aria-pressed={isCompleted}
                  aria-label={`${node.courseCode} ${course?.name ?? '과목 정보 없음'}, ${isCompleted ? '이수 완료' : '미이수'}, ${isRecommended ? '추천 과목' : '일반 과목'}${isPending ? ', 저장 중' : ''}`}
                  disabled={isLoading || isPending || !course}
                  onClick={() => onToggleCourse(node.courseCode)}
                >
                  {isRecommended && <span className="roadmap-node__recommend-badge">추천</span>}
                  <span className="roadmap-node__topline">
                    <strong>{node.courseCode}</strong>
                    <span className="roadmap-node__credits">{course ? `${course.credits}학점` : '정보 없음'}</span>
                  </span>
                  <span className="roadmap-node__name">{course?.name ?? '과목 정보 없음'}</span>
                  <span className="roadmap-node__meta">
                    {isPending
                      ? <span className="roadmap-node__pending">저장 중…</span>
                      : isCompleted
                        ? <span className="roadmap-node__completed"><b aria-hidden="true">✓</b> 이수 완료</span>
                        : <span>{isRecommended ? '추천 과목 · 미이수' : '미이수'}</span>}
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
