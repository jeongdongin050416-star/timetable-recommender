import type { CourseList } from '../types'
import { apiRequest } from './client'

export const coursesApi = {
  getAll: (signal?: AbortSignal) =>
    apiRequest<CourseList>('/api/courses', { signal }),
}
