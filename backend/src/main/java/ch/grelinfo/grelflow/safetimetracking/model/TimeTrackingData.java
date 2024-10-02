package ch.grelinfo.grelflow.safetimetracking.model;

/**
 * Time tracking data.
 *
 * @see TimeTrackingDataInterface
 */
public record TimeTrackingData(
    int originalEstimateSeconds,
    int remainingEstimateSeconds,
    int timeSpentSeconds
) implements TimeTrackingDataInterface {}
