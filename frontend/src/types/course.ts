export interface CourseSummary {
  courseCode: string
  name: string
  credits: number
  mainArea: string | null
}

export interface CourseList {
  courses: CourseSummary[]
}
