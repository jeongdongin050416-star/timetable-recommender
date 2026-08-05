export interface CourseSummary {
  courseCode: string
  name: string
  credits: number
}

export interface CourseList {
  courses: CourseSummary[]
}
