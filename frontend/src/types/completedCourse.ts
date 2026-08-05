import type { CourseSummary } from './course'

export type CompletedCourse = CourseSummary

export interface CompletedCourseList {
  userId: number
  courses: CompletedCourse[]
}

export interface CompletedCourseStatus {
  userId: number
  courseCode: string
  completed: boolean
}
