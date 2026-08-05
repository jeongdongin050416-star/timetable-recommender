import type { CompletedCourseList, CompletedCourseStatus } from '../types'
import { apiRequest } from './client'

const userCoursesPath = (userId: number) =>
  `/api/users/${encodeURIComponent(userId)}/completed-courses`

const coursePath = (userId: number, courseCode: string) =>
  `${userCoursesPath(userId)}/${encodeURIComponent(courseCode)}`

export const completedCoursesApi = {
  getAll: (userId: number) =>
    apiRequest<CompletedCourseList>(userCoursesPath(userId)),

  add: (userId: number, courseCode: string) =>
    apiRequest<CompletedCourseStatus>(coursePath(userId, courseCode), {
      method: 'PUT',
    }),

  remove: (userId: number, courseCode: string) =>
    apiRequest<CompletedCourseStatus>(coursePath(userId, courseCode), {
      method: 'DELETE',
    }),
}

