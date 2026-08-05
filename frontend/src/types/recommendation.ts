export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export interface MeetingTime {
  dayOfWeek: DayOfWeek
  startTime: string
  endTime: string
}

export interface TimetableCourse {
  courseCode: string
  name: string
  credits: number
  sectionKey: string
  meetingTimes: MeetingTime[]
}

export interface Timetable {
  score: number
  courseCount: number
  courses: TimetableCourse[]
}

export interface RecommendationParams {
  targetCourseCount: number
  interestedAreaIds?: number[]
  uninterestedAreaIds?: number[]
}

export interface RecommendationResult {
  userId: number
  targetCourseCount: number
  timetable: Timetable | null
}

