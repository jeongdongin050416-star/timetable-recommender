import type { RecommendationParams, RecommendationResult } from '../types'
import { apiRequest } from './client'

export const recommendationsApi = {
  get: (params: RecommendationParams, signal?: AbortSignal) => {
    const query = new URLSearchParams({
      targetCourseCount: String(params.targetCourseCount),
      studentYear: params.studentYear,
    })

    if (params.interestedAreaIds?.length) {
      query.set('interestedAreaIds', params.interestedAreaIds.join(','))
    }
    if (params.uninterestedAreaIds?.length) {
      query.set('uninterestedAreaIds', params.uninterestedAreaIds.join(','))
    }

    return apiRequest<RecommendationResult>(
      `/api/recommended-timetables?${query}`,
      { signal },
    )
  },
}
