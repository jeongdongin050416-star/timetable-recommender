import { useMemo, useState } from 'react'
import type {
  CourseSummary,
  RoadmapLayout,
} from '../../types'
import './RoadmapView.css'

const CANVAS_PADDING = 32
const NODE_WIDTH = 150
const NODE_HEIGHT = 76
const GROUP_GAP = 14
const NODE_GAP = 14
const LEFT_X = 16
const LEFT_WIDTH = 362
const RIGHT_X = 398
const RIGHT_WIDTH = 1064
const HALF_RIGHT_WIDTH = 522
const TOP_LEFT_WIDTH = 352

const FOUNDATION_REQUIRED = new Set(['CS101'])
const FOUNDATION_ELECTIVE = new Set(['CS109', 'MAS110', 'MAS109'])
const MAJOR_REQUIRED = new Set(['CS204', 'CS206', 'CS300', 'CS311', 'CS320', 'CS330'])
const KEY_MAJOR_ELECTIVE = new Set(['CS230', 'CS341'])

const AREA_META: Record<string, { title: string; color: string }> = {
  FOUNDATION_REQUIRED: { title: '기초필수', color: '#dce6f4' },
  FOUNDATION_ELECTIVE: { title: '기초선택', color: '#e8eef7' },
  MAJOR_REQUIRED: { title: '전공필수', color: '#cbd8ea' },
  KEY_MAJOR_ELECTIVE: { title: '주요 전공선택', color: '#e1e3e7' },
  DATA_SCIENCE: { title: '데이터 과학', color: '#f3a0a4' },
  SOFTWARE_DESIGN: { title: '소프트웨어 디자인', color: '#b9df91' },
  SYSTEM_NETWORK: { title: '시스템 · 네트워크', color: '#efad7d' },
  VISUAL_COMPUTING: { title: '비주얼 컴퓨팅', color: '#b2d19b' },
  THEORY: { title: '전산이론', color: '#f5dd96' },
  SECURE_COMPUTING: { title: '시큐어 컴퓨팅', color: '#b8b8b8' },
  SOCIAL_COMPUTING: { title: '소셜 컴퓨팅', color: '#aebbd2' },
  AI: { title: '인공지능', color: '#d8e5ef' },
  INTERACTIVE_COMPUTING: { title: '인터랙티브 컴퓨팅', color: '#cfddeb' },
  OTHER: { title: '기타', color: '#e5e7eb' },
}

function groupKey(course: CourseSummary) {
  if (FOUNDATION_REQUIRED.has(course.courseCode)) return 'FOUNDATION_REQUIRED'
  if (FOUNDATION_ELECTIVE.has(course.courseCode)) return 'FOUNDATION_ELECTIVE'
  if (MAJOR_REQUIRED.has(course.courseCode)) return 'MAJOR_REQUIRED'
  if (KEY_MAJOR_ELECTIVE.has(course.courseCode)) return 'KEY_MAJOR_ELECTIVE'
  return course.mainArea && AREA_META[course.mainArea] ? course.mainArea : 'OTHER'
}

function buildCourseLayout(courses: readonly CourseSummary[]): RoadmapLayout {
  const grouped = new Map<string, CourseSummary[]>()
  for (const course of courses) {
    const key = groupKey(course)
    grouped.set(key, [...(grouped.get(key) ?? []), course])
  }

  const groups: NonNullable<RoadmapLayout['groups']> = []
  const nodes: RoadmapLayout['nodes'] = []

  const addGroup = (key: string, x: number, y: number, width: number) => {
    const groupCourses = grouped.get(key)
    if (!groupCourses?.length) return 0
    const columnCount = Math.max(1, Math.floor((width - 32 + NODE_GAP) / (NODE_WIDTH + NODE_GAP)))
    const rowCount = Math.ceil(groupCourses.length / columnCount)
    const groupHeight = 48 + rowCount * NODE_HEIGHT + (rowCount - 1) * NODE_GAP + 16
    const meta = AREA_META[key]
    groups.push({ id: key.toLocaleLowerCase(), title: meta.title, x, y, width, height: groupHeight, color: meta.color })
    groupCourses.forEach((course, index) => {
      const column = index % columnCount
      const row = Math.floor(index / columnCount)
      nodes.push({
        courseCode: course.courseCode,
        x: x + 16 + column * (NODE_WIDTH + NODE_GAP),
        y: y + 36 + row * (NODE_HEIGHT + NODE_GAP),
        width: NODE_WIDTH,
        height: NODE_HEIGHT,
      })
    })
    return groupHeight
  }

  let leftY = 16
  for (const key of ['FOUNDATION_REQUIRED', 'FOUNDATION_ELECTIVE', 'MAJOR_REQUIRED', 'KEY_MAJOR_ELECTIVE']) {
    const height = addGroup(key, LEFT_X, leftY, LEFT_WIDTH)
    if (height) leftY += height + GROUP_GAP
  }

  let rightY = 16
  const addRightRow = (leftKey: string, rightKey: string, leftWidth = HALF_RIGHT_WIDTH) => {
    const rightWidth = RIGHT_WIDTH - leftWidth - GROUP_GAP
    const leftHeight = addGroup(leftKey, RIGHT_X, rightY, leftWidth)
    const rightHeight = addGroup(rightKey, RIGHT_X + leftWidth + GROUP_GAP, rightY, rightWidth)
    rightY += Math.max(leftHeight, rightHeight) + GROUP_GAP
  }

  addRightRow('DATA_SCIENCE', 'SOFTWARE_DESIGN', TOP_LEFT_WIDTH)
  addRightRow('SYSTEM_NETWORK', 'VISUAL_COMPUTING')
  rightY += addGroup('THEORY', RIGHT_X, rightY, RIGHT_WIDTH) + GROUP_GAP
  addRightRow('SECURE_COMPUTING', 'SOCIAL_COMPUTING')
  rightY += addGroup('AI', RIGHT_X, rightY, RIGHT_WIDTH) + GROUP_GAP
  rightY += addGroup('INTERACTIVE_COMPUTING', RIGHT_X, rightY, RIGHT_WIDTH) + GROUP_GAP
  addGroup('OTHER', RIGHT_X, rightY, RIGHT_WIDTH)

  return { groups, nodes, edges: [] }
}

export interface RoadmapViewProps {
  courses: readonly CourseSummary[]
  completedCourseCodes: ReadonlySet<string>
  pendingCourseCodes: ReadonlySet<string>
  recommendedCourseCodes?: ReadonlySet<string>
  isLoading?: boolean
  onToggleCourse: (courseCode: string) => void
  layout?: RoadmapLayout
}

export function RoadmapView({
  courses,
  completedCourseCodes,
  pendingCourseCodes,
  recommendedCourseCodes = new Set<string>(),
  isLoading = false,
  onToggleCourse,
  layout,
}: RoadmapViewProps) {
  const [query, setQuery] = useState('')
  const normalizedQuery = query.trim().toLocaleLowerCase()
  const visibleCourses = useMemo(
    () => courses.filter((course) => !/전산학\s*특강/.test(course.name)),
    [courses],
  )
  const resolvedLayout = useMemo(
    () => layout ?? buildCourseLayout(visibleCourses),
    [layout, visibleCourses],
  )

  const courseByCode = useMemo(
    () => new Map(visibleCourses.map((course) => [course.courseCode, course])),
    [visibleCourses],
  )
  const completedCourses = useMemo(
    () => visibleCourses.filter((course) => completedCourseCodes.has(course.courseCode)),
    [completedCourseCodes, visibleCourses],
  )
  const completedCredits = useMemo(
    () => completedCourses.reduce((total, course) => total + course.credits, 0),
    [completedCourses],
  )
  const matchCount = useMemo(
    () => normalizedQuery
      ? visibleCourses.filter(({ courseCode, name }) =>
          courseCode.toLocaleLowerCase().includes(normalizedQuery) ||
          name.toLocaleLowerCase().includes(normalizedQuery),
        ).length
      : visibleCourses.length,
    [visibleCourses, normalizedQuery],
  )
  const canvasSize = useMemo(
    () => ({
      width: Math.max(
        0,
        ...resolvedLayout.nodes.map((node) => node.x + node.width),
        ...(resolvedLayout.groups ?? []).map((group) => group.x + group.width),
      ) + CANVAS_PADDING,
      height: Math.max(
        0,
        ...resolvedLayout.nodes.map((node) => node.y + node.height),
        ...(resolvedLayout.groups ?? []).map((group) => group.y + group.height),
      ) + CANVAS_PADDING,
    }),
    [resolvedLayout.groups, resolvedLayout.nodes],
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
          <div><dt>전체 과목</dt><dd>{visibleCourses.length}개</dd></div>
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
            {(resolvedLayout.groups ?? []).map((group) => (
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

          <div className="roadmap-view__nodes">
            {resolvedLayout.nodes.map((node) => {
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
