import type {
  DayOfWeek,
  MeetingTime,
  Timetable,
  TimetableCourse,
} from '../../types'

const WEEKDAYS: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
]
const WEEKEND: DayOfWeek[] = ['SATURDAY', 'SUNDAY']
const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: '월요일',
  TUESDAY: '화요일',
  WEDNESDAY: '수요일',
  THURSDAY: '목요일',
  FRIDAY: '금요일',
  SATURDAY: '토요일',
  SUNDAY: '일요일',
}
const MIN_START_MINUTES = 8 * 60
const MIN_END_MINUTES = 22 * 60
const SLOT_MINUTES = 30
const SLOT_HEIGHT = 42

const COURSE_COLORS = [
  { backgroundColor: '#dbeafe', borderColor: '#2563eb' },
  { backgroundColor: '#dcfce7', borderColor: '#16a34a' },
  { backgroundColor: '#fef3c7', borderColor: '#d97706' },
  { backgroundColor: '#f3e8ff', borderColor: '#9333ea' },
  { backgroundColor: '#ffe4e6', borderColor: '#e11d48' },
  { backgroundColor: '#cffafe', borderColor: '#0891b2' },
]

interface ParsedMeeting {
  course: TimetableCourse
  meeting: MeetingTime
  start: number
  end: number
  lane: number
  laneCount: number
}

interface TimetableGridProps {
  timetable: Timetable
}

function parseTime(time: string): number | null {
  const match = /^(\d{2}):(\d{2}):(\d{2})$/.exec(time)
  if (!match) {
    return null
  }

  const hours = Number(match[1])
  const minutes = Number(match[2])
  const seconds = Number(match[3])
  if (hours > 23 || minutes > 59 || seconds > 59) {
    return null
  }
  return hours * 60 + minutes + seconds / 60
}

function formatMinutes(totalMinutes: number) {
  const hours = Math.floor(totalMinutes / 60)
  const minutes = Math.floor(totalMinutes % 60)
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
}

function formatApiTime(time: string) {
  return time.slice(0, 5)
}

function getCourseColors(courseCode: string) {
  let hash = 0
  for (const character of courseCode) {
    hash = (hash * 31 + character.charCodeAt(0)) >>> 0
  }
  return COURSE_COLORS[hash % COURSE_COLORS.length]
}

export function getTimetableProblem(timetable: Timetable): string | null {
  if (!Array.isArray(timetable.courses) || timetable.courses.length === 0) {
    return '추천 결과에 표시할 과목이 없습니다.'
  }

  for (const course of timetable.courses) {
    if (!course.courseCode || !course.name || !course.sectionKey) {
      return '추천 결과에 필수 과목 정보가 누락되었습니다.'
    }
    if (!Array.isArray(course.meetingTimes) || course.meetingTimes.length === 0) {
      return `${course.courseCode} 과목의 수업 시간 정보가 없습니다.`
    }
    for (const meeting of course.meetingTimes) {
      const start = parseTime(meeting.startTime)
      const end = parseTime(meeting.endTime)
      if (
        !DAY_LABELS[meeting.dayOfWeek] ||
        start === null ||
        end === null ||
        start >= end
      ) {
        return `${course.courseCode} 과목의 수업 시간 형식이 올바르지 않습니다.`
      }
    }
  }
  return null
}

function createMeetings(timetable: Timetable): ParsedMeeting[] {
  const meetings = timetable.courses.flatMap((course) =>
    course.meetingTimes.map((meeting) => ({
      course,
      meeting,
      start: parseTime(meeting.startTime) ?? 0,
      end: parseTime(meeting.endTime) ?? 0,
      lane: 0,
      laneCount: 1,
    })),
  )

  for (const day of [...WEEKDAYS, ...WEEKEND]) {
    const dayMeetings = meetings
      .filter(({ meeting }) => meeting.dayOfWeek === day)
      .sort((left, right) => left.start - right.start || left.end - right.end)
    const laneEndTimes: number[] = []

    for (const meeting of dayMeetings) {
      const availableLane = laneEndTimes.findIndex((end) => end <= meeting.start)
      meeting.lane = availableLane === -1 ? laneEndTimes.length : availableLane
      laneEndTimes[meeting.lane] = meeting.end
    }
    for (const meeting of dayMeetings) {
      meeting.laneCount = Math.max(1, laneEndTimes.length)
    }
  }
  return meetings
}

export function TimetableGrid({ timetable }: TimetableGridProps) {
  const meetings = createMeetings(timetable)
  const presentDays = new Set(meetings.map(({ meeting }) => meeting.dayOfWeek))
  const days = [
    ...WEEKDAYS,
    ...WEEKEND.filter((day) => presentDays.has(day)),
  ]
  const startMinutes = Math.min(
    MIN_START_MINUTES,
    Math.floor(Math.min(...meetings.map(({ start }) => start)) / SLOT_MINUTES) *
      SLOT_MINUTES,
  )
  const endMinutes = Math.max(
    MIN_END_MINUTES,
    Math.ceil(Math.max(...meetings.map(({ end }) => end)) / SLOT_MINUTES) *
      SLOT_MINUTES,
  )
  const ticks = Array.from(
    { length: (endMinutes - startMinutes) / SLOT_MINUTES + 1 },
    (_, index) => startMinutes + index * SLOT_MINUTES,
  )
  const gridHeight = ((endMinutes - startMinutes) / SLOT_MINUTES) * SLOT_HEIGHT
  const totalCredits = timetable.courses.reduce(
    (total, course) => total + course.credits,
    0,
  )

  return (
    <section className="timetable-result" aria-labelledby="timetable-result-title">
      <div className="timetable-result__heading">
        <div>
          <p className="timetable-result__eyebrow">RECOMMENDED SCHEDULE</p>
          <h2 id="timetable-result-title">추천 시간표</h2>
        </div>
        <dl className="timetable-summary">
          <div><dt>추천 점수</dt><dd>{timetable.score}점</dd></div>
          <div><dt>과목 수</dt><dd>{timetable.courseCount}개</dd></div>
          <div><dt>총 학점</dt><dd>{totalCredits}학점</dd></div>
        </dl>
      </div>

      <div className="timetable-scroll" tabIndex={0} aria-label="주간 추천 시간표">
        <div
          className="timetable-grid"
          style={{ gridTemplateColumns: `72px repeat(${days.length}, minmax(160px, 1fr))` }}
        >
          <div className="timetable-grid__corner">시간</div>
          {days.map((day) => (
            <div className="timetable-grid__day-header" key={day}>
              {DAY_LABELS[day]}
            </div>
          ))}

          <div className="timetable-grid__time-column" style={{ height: gridHeight }}>
            {ticks.map((tick) => (
              <time
                key={tick}
                dateTime={formatMinutes(tick)}
                style={{ top: ((tick - startMinutes) / SLOT_MINUTES) * SLOT_HEIGHT }}
              >
                {formatMinutes(tick)}
              </time>
            ))}
          </div>

          {days.map((day) => (
            <div
              className="timetable-grid__day-column"
              style={{ height: gridHeight }}
              key={day}
            >
              {meetings
                .filter(({ meeting }) => meeting.dayOfWeek === day)
                .map(({ course, meeting, start, end, lane, laneCount }, index) => {
                  const laneWidth = 100 / laneCount
                  return (
                    <article
                      className="timetable-event"
                      key={`${course.sectionKey}-${meeting.startTime}-${index}`}
                      style={{
                        top: ((start - startMinutes) / SLOT_MINUTES) * SLOT_HEIGHT + 2,
                        height: Math.max(
                          ((end - start) / SLOT_MINUTES) * SLOT_HEIGHT - 4,
                          28,
                        ),
                        left: `calc(${lane * laneWidth}% + 3px)`,
                        width: `calc(${laneWidth}% - 6px)`,
                        ...getCourseColors(course.courseCode),
                      }}
                    >
                      <strong>{course.courseCode}</strong>
                      <span className="timetable-event__name">{course.name}</span>
                      <span>{course.sectionKey}</span>
                      <time>
                        {formatApiTime(meeting.startTime)}–{formatApiTime(meeting.endTime)}
                      </time>
                    </article>
                  )
                })}
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

