package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimeTracking(
    @NotNull TrackingStatus status,
    @NotNull int completionPercentage,
    @NotNull int plannedUsagePercentage,
    @NotNull int plannedTimeSeconds,
    @NotNull int spentTimeSeconds,
    @NotNull int remainingTimeSeconds
) implements TimeTrackingDataInterface {}