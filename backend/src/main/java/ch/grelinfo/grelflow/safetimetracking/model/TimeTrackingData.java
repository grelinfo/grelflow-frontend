package ch.grelinfo.grelflow.safetimetracking.model;


public record TimeTrackingData(
    int plannedTimeSeconds,
    int spentTimeSeconds,
    int remainingTimeSeconds
) implements TimeTrackingDataInterface {}
