export interface RoadmapNode {
  courseCode: string
  x: number
  y: number
  width: number
  height: number
}

export type RoadmapRelationType = 'PREREQUISITE' | 'RECOMMENDED'

export interface RoadmapEdge {
  from: string
  to: string
  relationType: RoadmapRelationType
}

export interface RoadmapLayout {
  nodes: RoadmapNode[]
  edges: RoadmapEdge[]
}
