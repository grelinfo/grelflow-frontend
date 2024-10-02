export enum WorkItemStatus {
  TODO = "todo",
  INPROGRESS = "inprogress",
  DONE = "done",
  UNKNOWN = "unknown"
}

export enum TrackingStatus {
  ONTRACK = "ontrack",
  UNDERSPENT = "underspent",
  OVERSPENT = "overspent"
}

export enum WorkItemType {
  FEATURE = "Feature",
  STORY = "Story",
  BUG = "Bug",
  UNKNOWN = "Unknown"
}

interface TimeTracking {
  status: TrackingStatus;
  usagePercentage: number;
  originalEstimateSeconds: number;
  remainingEstimateSeconds: number;
  timeSpentSeconds: number;
  estimatedStatus?: TrackingStatus;
  estimatedCompletionPercentage?: number;
  estimatedUsagePercentage?: number;
}

interface WorkItem {
  id: string;
  type: WorkItemType;
  status: WorkItemStatus;
  timeTracking: TimeTracking;
  warnings?: string[];
}

interface Feature {
  id: string;
  recordedTimestamp: string; // ISO 8601 datetime
  name?: string;
  status: WorkItemStatus;
  timeTracking: TimeTracking;
  workItems?: WorkItem[];
  warnings?: string[];
}

export type { Feature };