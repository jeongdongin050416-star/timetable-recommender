import { apiRequest } from './client'

export interface HealthStatus {
  status: 'ok'
}

export const healthApi = {
  getStatus: () => apiRequest<HealthStatus>('/api/health'),
}

