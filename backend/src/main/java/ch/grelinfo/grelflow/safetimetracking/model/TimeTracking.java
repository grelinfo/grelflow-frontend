package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

/**
 * Represents time tracking information for a work item.
 *
 * @param status The current status of the work item.
 * @param usagePercentage The percentage of time used so far.
 * @param originalEstimateSeconds The estimated time in seconds.
 * @param timeSpentSeconds The spent time in seconds.
 * @param remainingEstimateSeconds The remaining time in seconds.
 * @param estimatedStatus The projected status based on current and future estimations. Null when the work item is done.
 * @param estimatedCompletionPercentage The estimated completion percentage. Null when the work item is done.
 * @param estimatedUsagePercentage The estimated usage percentage. Null when the work item is done.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimeTracking(
    @NotNull TrackingStatus status,
    @NotNull int usagePercentage,
    @NotNull int originalEstimateSeconds,
    @NotNull int remainingEstimateSeconds,
    @NotNull int timeSpentSeconds,
    TrackingStatus estimatedStatus,
    Integer estimatedCompletionPercentage,
    Integer estimatedUsagePercentage
) implements TimeTrackingDataInterface {}