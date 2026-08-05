import { useEffect, useRef, useState } from 'react'
import { ApiError, recommendationsApi } from '../../api'
import type { Timetable } from '../../types'
import {
  RecommendationForm,
  type RecommendationRequest,
} from './RecommendationForm'
import { getTimetableProblem, TimetableGrid } from './TimetableGrid'
import './timetable.css'

type PageState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; timetable: Timetable }
  | { status: 'empty'; targetCourseCount: number }
  | { status: 'invalid'; message: string }
  | { status: 'error'; code: string; message: string; isNetworkError: boolean }

export function TimetablePage() {
  const [state, setState] = useState<PageState>({ status: 'idle' })
  const activeRequest = useRef<AbortController | null>(null)

  useEffect(
    () => () => {
      activeRequest.current?.abort()
    },
    [],
  )

  const requestRecommendation = async ({
    userId,
    params,
  }: RecommendationRequest) => {
    activeRequest.current?.abort()
    const controller = new AbortController()
    activeRequest.current = controller
    setState({ status: 'loading' })

    try {
      const result = await recommendationsApi.get(userId, params, controller.signal)
      if (controller.signal.aborted) {
        return
      }

      if (result.timetable === null) {
        setState({
          status: 'empty',
          targetCourseCount: result.targetCourseCount,
        })
        return
      }

      const problem = getTimetableProblem(result.timetable)
      if (problem) {
        setState({ status: 'invalid', message: problem })
        return
      }
      setState({ status: 'success', timetable: result.timetable })
    } catch (error) {
      if (controller.signal.aborted) {
        return
      }

      if (error instanceof ApiError) {
        setState({
          status: 'error',
          code: error.code,
          message: error.message,
          isNetworkError: error.code === 'NETWORK_ERROR',
        })
      } else {
        setState({
          status: 'error',
          code: 'UNKNOWN_ERROR',
          message: '추천 시간표를 처리하는 중 알 수 없는 오류가 발생했습니다.',
          isNetworkError: false,
        })
      }
    } finally {
      if (activeRequest.current === controller) {
        activeRequest.current = null
      }
    }
  }

  return (
    <main className="timetable-page">
      <header className="timetable-page__intro">
        <p className="timetable-page__eyebrow">TIMETABLE RECOMMENDER</p>
        <h1>나에게 맞는 시간표 찾기</h1>
        <p>이수 과목과 관심 분야를 바탕으로 충돌 없는 과목 조합을 추천합니다.</p>
      </header>

      <section className="recommendation-panel" aria-labelledby="condition-title">
        <div className="recommendation-panel__heading">
          <div>
            <span>STEP 1</span>
            <h2 id="condition-title">추천 조건</h2>
          </div>
          <p>관심 또는 미관심 분야를 여러 개 선택할 수 있습니다.</p>
        </div>
        <RecommendationForm
          isLoading={state.status === 'loading'}
          onSubmit={(request) => void requestRecommendation(request)}
        />
      </section>

      <div className="timetable-page__output" aria-live="polite">
        {state.status === 'idle' && (
          <div className="result-state result-state--idle">
            <strong>추천 조건을 입력해 주세요.</strong>
            <span>조회 버튼을 누르면 추천 시간표가 이곳에 표시됩니다.</span>
          </div>
        )}
        {state.status === 'loading' && (
          <div className="result-state result-state--loading" role="status">
            <span className="result-state__spinner" aria-hidden="true" />
            <strong>추천 시간표를 계산하고 있습니다.</strong>
            <span>조건에 맞는 충돌 없는 조합을 찾는 중입니다.</span>
          </div>
        )}
        {state.status === 'empty' && (
          <div className="result-state result-state--empty">
            <strong>조건에 맞는 시간표를 만들 수 없습니다.</strong>
            <span>
              정확히 {state.targetCourseCount}개 과목으로 구성된 충돌 없는 조합이 없습니다.
              이수 과목이나 관심 분야, 목표 과목 수를 조정해 보세요.
            </span>
          </div>
        )}
        {state.status === 'invalid' && (
          <div className="result-state result-state--error" role="alert">
            <strong>시간표 응답을 표시할 수 없습니다.</strong>
            <span>{state.message}</span>
          </div>
        )}
        {state.status === 'error' && (
          <div className="result-state result-state--error" role="alert">
            <strong>
              {state.isNetworkError
                ? '백엔드 서버에 연결할 수 없습니다.'
                : '추천 시간표 조회에 실패했습니다.'}
            </strong>
            <span>{state.message}</span>
            <code>{state.code}</code>
          </div>
        )}
        {state.status === 'success' && <TimetableGrid timetable={state.timetable} />}
      </div>
    </main>
  )
}
