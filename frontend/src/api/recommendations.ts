import type { RecommendationParams, RecommendationResult } from '../types'
import { apiRequest } from './client'

export const recommendationsApi = {
  get: (userId: number, params: RecommendationParams) => {
    const query = new URLSearchParams({
      targetCourseCount: String(params.targetCourseCount),
    })

    if (params.interestedAreaIds?.length) {
      query.set('interestedAreaIds', params.interestedAreaIds.join(','))
    }
    if (params.uninterestedAreaIds?.length) {
      query.set('uninterestedAreaIds', params.uninterestedAreaIds.join(','))
    }

    return apiRequest<RecommendationResult>(
      `/api/users/${encodeURIComponent(userId)}/recommended-timetables?${query}`,
    )
  },
}

