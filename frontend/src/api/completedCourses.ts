import type { CompletedCourseList, CompletedCourseStatus } from '../types'
import { apiRequest } from './client'

const userCoursesPath = '/api/completed-courses'

const coursePath = (courseCode: string) =>
  `${userCoursesPath}/${encodeURIComponent(courseCode)}`

export const completedCoursesApi = {
  getAll: () => apiRequest<CompletedCourseList>(userCoursesPath),

  add: (courseCode: string) =>
    apiRequest<CompletedCourseStatus>(coursePath(courseCode), {
      method: 'PUT',
    }),

  remove: (courseCode: string) =>
    apiRequest<CompletedCourseStatus>(coursePath(courseCode), {
      method: 'DELETE',
    }),
}
