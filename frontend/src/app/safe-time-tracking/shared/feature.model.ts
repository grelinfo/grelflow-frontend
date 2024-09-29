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

interface BudgetTracking {
  status: TrackingStatus;
  budgetUsagePercentage: number;
  budgetDeviationPercentage: number;
  budgetSeconds: number;
  budgetRemainingSeconds: number;
}

interface TimeTracking {
  status: TrackingStatus;
  completionPercentage: number;
  plannedUsagePercentage: number;
  plannedTimeSeconds: number;
  spentTimeSeconds: number;
  remainingTimeSeconds: number;
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
  budgetTracking: BudgetTracking;
  timeTracking: TimeTracking;
  workItems?: WorkItem[];
  warnings?: string[];
}

export type { Feature };