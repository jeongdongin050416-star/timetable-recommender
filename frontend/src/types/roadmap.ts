export interface RoadmapNode {
  courseCode: string
  x: number
  y: number
  width: number
  height: number
}

export interface RoadmapGroup {
  id: string
  title: string
  x: number
  y: number
  width: number
  height: number
  color: string
}

export type RoadmapRelationType = 'PREREQUISITE' | 'RECOMMENDED'

export interface RoadmapEdge {
  from: string
  to: string
  relationType: RoadmapRelationType
}

export interface RoadmapLayout {
  groups?: RoadmapGroup[]
  nodes: RoadmapNode[]
  edges: RoadmapEdge[]
}
