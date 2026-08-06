export interface InterestAreaOption {
  id: number
  name: string
}

// V1__create_schema.sql의 interest_area INSERT 순서와 일치한다.
export const INTEREST_AREAS: readonly InterestAreaOption[] = [
  { id: 1, name: '데이터과학' },
  { id: 2, name: '시스템-네트워크' },
  { id: 3, name: '전산이론' },
  { id: 4, name: '소프트웨어디자인' },
  { id: 5, name: '시큐어컴퓨팅' },
  { id: 6, name: '비주얼컴퓨팅' },
  { id: 7, name: '인공지능' },
  { id: 8, name: '소셜컴퓨팅' },
  { id: 9, name: '인터랙티브컴퓨팅' },
]
