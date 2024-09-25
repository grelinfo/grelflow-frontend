package ch.grelinfo.grelflow.safetimetracking.model;

public interface TimeTrackingDataInterface {
    int plannedTimeSeconds();
    int spentTimeSeconds();
    int remainingTimeSeconds();
}
