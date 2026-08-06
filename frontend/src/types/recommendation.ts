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

export type StudentYear =
  | 'FIRST_YEAR'
  | 'SECOND_YEAR'
  | 'THIRD_YEAR'
  | 'FOURTH_YEAR_OR_ABOVE'

export interface RecommendationParams {
  targetCourseCount: number
  studentYear: StudentYear
  interestedAreaIds?: number[]
  uninterestedAreaIds?: number[]
}

export interface RecommendationResult {
  userId: number
  targetCourseCount: number
  studentYear: StudentYear
  timetable: Timetable | null
}
