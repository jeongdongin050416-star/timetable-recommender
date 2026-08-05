import { useState, type FormEvent } from 'react'
import type { RecommendationParams } from '../../types'
import { INTEREST_AREAS } from './interestAreas'

export interface RecommendationRequest {
  params: RecommendationParams
}

interface RecommendationFormProps {
  isLoading: boolean
  hasResult?: boolean
  onSubmit: (request: RecommendationRequest) => void
}

interface FormValues {
  targetCourseCount: string
}

const INITIAL_VALUES: FormValues = {
  targetCourseCount: '3',
}

function parsePositiveInteger(value: string, label: string) {
  const normalized = value.trim()
  if (!/^\d+$/.test(normalized)) {
    throw new Error(`${label}에는 양의 정수를 입력해 주세요.`)
  }

  const number = Number(normalized)
  if (!Number.isSafeInteger(number) || number < 1) {
    throw new Error(`${label}에는 양의 정수를 입력해 주세요.`)
  }
  return number
}

export function RecommendationForm({
  isLoading,
  hasResult = false,
  onSubmit,
}: RecommendationFormProps) {
  const [values, setValues] = useState(INITIAL_VALUES)
  const [interestedAreaIds, setInterestedAreaIds] = useState<Set<number>>(
    () => new Set(),
  )
  const [uninterestedAreaIds, setUninterestedAreaIds] = useState<Set<number>>(
    () => new Set(),
  )
  const [validationError, setValidationError] = useState<string | null>(null)

  const updateValue = (name: keyof FormValues, value: string) => {
    setValues((current) => ({ ...current, [name]: value }))
    setValidationError(null)
  }

  const toggleArea = (
    areaId: number,
    category: 'interested' | 'uninterested',
  ) => {
    const selectedIds =
      category === 'interested' ? interestedAreaIds : uninterestedAreaIds
    const shouldSelect = !selectedIds.has(areaId)
    const updateSelected =
      category === 'interested' ? setInterestedAreaIds : setUninterestedAreaIds
    const updateOpposite =
      category === 'interested' ? setUninterestedAreaIds : setInterestedAreaIds

    updateSelected((current) => {
      const next = new Set(current)
      if (shouldSelect) {
        next.add(areaId)
      } else {
        next.delete(areaId)
      }
      return next
    })
    if (shouldSelect) {
      updateOpposite((current) => {
        const next = new Set(current)
        next.delete(areaId)
        return next
      })
    }
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    try {
      const targetCourseCount = parsePositiveInteger(
        values.targetCourseCount,
        '목표 과목 수',
      )
      if (targetCourseCount > 20) {
        throw new Error('목표 과목 수는 1개부터 20개까지 입력해 주세요.')
      }

      setValidationError(null)
      onSubmit({
        params: {
          targetCourseCount,
          interestedAreaIds:
            interestedAreaIds.size > 0
              ? [...interestedAreaIds].sort((left, right) => left - right)
              : undefined,
          uninterestedAreaIds:
            uninterestedAreaIds.size > 0
              ? [...uninterestedAreaIds].sort((left, right) => left - right)
              : undefined,
        },
      })
    } catch (error) {
      setValidationError(
        error instanceof Error ? error.message : '입력 값을 확인해 주세요.',
      )
    }
  }

  return (
    <form className="recommendation-form" onSubmit={handleSubmit} noValidate>
      <div className="recommendation-form__fields">
        <label>
          <span>목표 과목 수</span>
          <input
            name="targetCourseCount"
            type="number"
            min="1"
            max="20"
            step="1"
            inputMode="numeric"
            value={values.targetCourseCount}
            onChange={(event) =>
              updateValue('targetCourseCount', event.target.value)
            }
            disabled={isLoading}
          />
        </label>
      </div>

      <div className="recommendation-form__area-groups">
        <fieldset disabled={isLoading}>
          <legend>
            관심 분야 <span>{interestedAreaIds.size}개 선택</span>
          </legend>
          <p>추천 점수에 긍정적으로 반영할 분야입니다.</p>
          <div className="area-options">
            {INTEREST_AREAS.map((area) => (
              <label className="area-option" key={area.id}>
                <input
                  type="checkbox"
                  checked={interestedAreaIds.has(area.id)}
                  onChange={() => toggleArea(area.id, 'interested')}
                />
                <span className="area-option__body">
                  <span className="area-option__check" aria-hidden="true">✓</span>
                  {area.name}
                </span>
              </label>
            ))}
          </div>
        </fieldset>

        <fieldset disabled={isLoading}>
          <legend>
            미관심 분야 <span>{uninterestedAreaIds.size}개 선택</span>
          </legend>
          <p>추천 점수를 낮게 반영할 분야입니다.</p>
          <div className="area-options">
            {INTEREST_AREAS.map((area) => (
              <label className="area-option area-option--negative" key={area.id}>
                <input
                  type="checkbox"
                  checked={uninterestedAreaIds.has(area.id)}
                  onChange={() => toggleArea(area.id, 'uninterested')}
                />
                <span className="area-option__body">
                  <span className="area-option__check" aria-hidden="true">✓</span>
                  {area.name}
                </span>
              </label>
            ))}
          </div>
        </fieldset>
      </div>

      {validationError && (
        <p className="recommendation-form__error" role="alert">
          {validationError}
        </p>
      )}

      <button type="submit" disabled={isLoading}>
        {isLoading
          ? '추천 시간표 계산 중…'
          : hasResult
            ? '추천 시간표 다시 조회'
            : '추천 시간표 조회'}
      </button>
    </form>
  )
}
