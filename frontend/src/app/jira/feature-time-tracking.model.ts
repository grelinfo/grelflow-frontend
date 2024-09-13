export type FeatureTimeTracking = {
    issueKey: string;
    timestamp: string
    originalEstimateSeconds: number;
    computedEstimateSeconds: number;
    computedTimeSpentSeconds: number;
    computedRemainingEstimateSeconds: number;
}