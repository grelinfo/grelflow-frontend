package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingData;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTracking;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingDataInterface;
import ch.grelinfo.grelflow.safetimetracking.model.TrackingStatus;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItemStatus;
import java.util.List;
import java.util.function.Consumer;

public class TimeTrackingBuilder {

    private static final List<Float> MODIFIED_FIBONACCI = List.of(0.0f, 0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 8.0f, 13.0f, 20.0f, 40.0f, 100.0f);

    private Integer storyPointsToSecondsFactor;
    private Integer allowedTimeDeviationPercentage;
    private WorkItemStatus workItemStatus;
    private Float storyPoints;
    private Integer plannedTimeSeconds;
    private Integer remainingTimeSeconds;
    private Integer timeSpentSeconds;
    private Consumer<String> warningConsumer;

    public TimeTrackingBuilder setStoryPointsToSecondsFactor(int storyPointsToSecondsFactor) {
        this.storyPointsToSecondsFactor = mapIfEmptyOrThrow(this.storyPointsToSecondsFactor, storyPointsToSecondsFactor, "storyPointsToSecondsFactor");
        return this;
    }

    public TimeTrackingBuilder setAllowedTimeDeviationPercentage(int allowedTimeDeviationPercentage) {
        this.allowedTimeDeviationPercentage = mapIfEmptyOrThrow(this.allowedTimeDeviationPercentage, allowedTimeDeviationPercentage, "allowedTimeDeviationPercentage");
        return this;
    }

    public TimeTrackingBuilder setStoryPoints(float storyPoints) {
        this.storyPoints = mapIfEmptyOrThrow(this.storyPoints, storyPoints, "storyPoints");
        return this;
    }

    public TimeTrackingBuilder setPlannedTimeSeconds(int plannedTimeSeconds) {
        this.plannedTimeSeconds = mapIfEmptyOrThrow(this.plannedTimeSeconds, plannedTimeSeconds, "plannedTimeSeconds");
        return this;
    }

    public TimeTrackingBuilder setRemainingTimeSeconds(int remainingEstimateSeconds) {
        this.remainingTimeSeconds = mapIfEmptyOrThrow(this.remainingTimeSeconds, remainingEstimateSeconds, "remainingTimeSeconds");
        return this;
    }

    public TimeTrackingBuilder setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = mapIfEmptyOrThrow(this.timeSpentSeconds, timeSpentSeconds, "timeSpentSeconds");
        return this;
    }

    public TimeTrackingBuilder setWorkItemStatus(WorkItemStatus workItemStatus) {
        this.workItemStatus = mapIfEmptyOrThrow(this.workItemStatus, workItemStatus, "workItemStatus");
        return this;
    }

    public TimeTrackingBuilder doOnWarning(Consumer<String> consumer) {
        this.warningConsumer = mapIfEmptyOrThrow(this.warningConsumer, consumer, "warningConsumer");
        return this;
    }

    public TimeTracking build() {
        boolean isTimeTrackingPresent = isTimeTrackingPresent();
        int plannedTimeSeconds = getPlannedTimeSeconds(isTimeTrackingPresent);
        int timeSpentSeconds = getTimeSpentSeconds();
        int remainingTimeSeconds = calculateRemainingTimeSeconds(plannedTimeSeconds, timeSpentSeconds, isTimeTrackingPresent);
        int plannedTimeUsageSeconds = calculatePlannedTimeUsageSeconds(remainingTimeSeconds, timeSpentSeconds);

        warnIfPlannedTimeFarFromStoryPoint();
        warnIfEitherStoryPointsOrOriginalEstimateIsMissing();
        warnIfTimeTrackingIsMissing(isTimeTrackingPresent);

        return new TimeTracking(
            getStatus(plannedTimeSeconds, plannedTimeUsageSeconds),
            getCompletionPercentage(plannedTimeSeconds, timeSpentSeconds),
            calculatePlannedUsagePercentage(plannedTimeSeconds, plannedTimeUsageSeconds),
            plannedTimeSeconds,
            timeSpentSeconds,
            remainingTimeSeconds
        );
    }

    public TimeTrackingData data() {
        boolean isTimeTrackingPresent = isTimeTrackingPresent();
        int plannedTimeSeconds = getPlannedTimeSeconds(isTimeTrackingPresent);
        int timeSpentSeconds = getTimeSpentSeconds();

        return new TimeTrackingData(
            plannedTimeSeconds,
            timeSpentSeconds,
            calculateRemainingTimeSeconds(plannedTimeSeconds, timeSpentSeconds, isTimeTrackingPresent)
        );
    }

    private int calculateStoryPointSeconds() {
        if (storyPointsToSecondsFactor == null || storyPoints == null) {
            return 0;
        }
        return (int) (storyPoints * storyPointsToSecondsFactor);
    }

    private float calculatePlannedTimeToStoryPoints() {
        if (storyPointsToSecondsFactor == null || plannedTimeSeconds == null || storyPointsToSecondsFactor == 0 || plannedTimeSeconds == 0) {
            return 0;
        }
        return (float) ((double) plannedTimeSeconds / storyPointsToSecondsFactor);
    }

    private static float nextSmallerFibonacci(float number) {
        return MODIFIED_FIBONACCI.stream().filter(fibonacci -> fibonacci < number).reduce((first, second) -> second).orElse(number);
    }

    private static float nextBiggerFibonacci(float number) {
        return MODIFIED_FIBONACCI.stream().filter(fibonacci -> fibonacci > number).findFirst().orElse(number);
    }

    private <T> T mapIfEmptyOrThrow(T field, T value, String name) {
        if (field != null) {
            throw new IllegalStateException(name + " already set.");
        }
        return value;
    }

    private int getCompletionPercentage(int plannedTimeSeconds, int timeSpentSeconds) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return 100;
        }
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.TODO)) {
            return 0;
        }
        if (plannedTimeSeconds == 0) {
            return 0;
        }
        return (int) (((double) timeSpentSeconds / plannedTimeSeconds) * 100);
    }

    private int getPlannedTimeSeconds(boolean isTimeTrackingPresent) {
        return plannedTimeSeconds != null && isTimeTrackingPresent ? plannedTimeSeconds : calculateStoryPointSeconds();
    }

    private void warnIfPlannedTimeFarFromStoryPoint() {
        if (warningConsumer == null || storyPoints == null || plannedTimeSeconds == null) {
            return;
        }
        float plannedTimeToStoryPoints = calculatePlannedTimeToStoryPoints();
        float largerFibonacci = nextBiggerFibonacci(storyPoints);
        float smallerFibonacci = nextSmallerFibonacci(storyPoints);

        if (plannedTimeToStoryPoints > largerFibonacci) {
            warningConsumer.accept("Planned time is significantly higher than expected for %.1f story points.".formatted(storyPoints));
        }
        if (plannedTimeToStoryPoints < smallerFibonacci) {
            warningConsumer.accept("Planned time is significantly lower than expected for %.1f story points.".formatted(storyPoints));
        }
    }
    private void warnIfEitherStoryPointsOrOriginalEstimateIsMissing() {
        if (warningConsumer != null && storyPoints == null && plannedTimeSeconds == null) {
            warningConsumer.accept("Story points and original estimate are missing.");
        }
    }

    private void warnIfTimeTrackingIsMissing(boolean isTimeTrackingPresent) {
        if (warningConsumer != null && !isTimeTrackingPresent && (workItemStatus == null || !workItemStatus.equals(WorkItemStatus.DONE))) {
            warningConsumer.accept("Time tracking data are missing.");
        }
    }

    private int getTimeSpentSeconds() {
        return timeSpentSeconds != null ? timeSpentSeconds : 0;
    }

    private boolean isTimeTrackingPresent() {
     return (plannedTimeSeconds != null && plannedTimeSeconds != 0) ||
       (remainingTimeSeconds != null && remainingTimeSeconds != 0) ||
       (timeSpentSeconds != null && timeSpentSeconds != 0);
    }

    private TrackingStatus getStatus(int plannedTimeSeconds, int plannedTimeUsageSeconds) {
        int timeDeviationPercentage = calculateTimeDeviationPercentage(plannedTimeSeconds, plannedTimeUsageSeconds);
        int allowedTimeDeviationPercentage = this.allowedTimeDeviationPercentage != null ? this.allowedTimeDeviationPercentage : 0;
        if (timeDeviationPercentage > allowedTimeDeviationPercentage) {
            return TrackingStatus.OVERSPENT;
        }
        if (timeDeviationPercentage < -allowedTimeDeviationPercentage) {
            return TrackingStatus.UNDERSPENT;
        }
        return TrackingStatus.ONTRACK;
    }

    private int calculateRemainingTimeSeconds(int plannedTimeSeconds, int timeSpentSeconds, boolean isTimeTrackingPresent) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return 0;
        }
        return remainingTimeSeconds != null && isTimeTrackingPresent ? remainingTimeSeconds : (plannedTimeSeconds - timeSpentSeconds);
    }

    private static int calculatePlannedUsagePercentage(int plannedTimeSeconds, int plannedTimeUsageSeconds) {
        return calculatePercentage(plannedTimeUsageSeconds, plannedTimeSeconds);
    }

    private static int calculatePlannedTimeUsageSeconds(int remainingTimeSeconds, int timeSpentSeconds) {
        return timeSpentSeconds + remainingTimeSeconds;
    }

    /**
     * Calculate the deviation percentage of the planned time usage.
     *
     * @param plannedTimeSeconds the planned time in seconds
     * @param plannedTimeUsageSeconds the planned time usage in seconds
     * @return the deviation percentage
     */
    private static int calculateTimeDeviationPercentage(int plannedTimeSeconds, int plannedTimeUsageSeconds) {
        return calculatePercentage(plannedTimeUsageSeconds, plannedTimeSeconds) - 100;
    }

    private static int calculatePercentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }
        return (int) (((double) numerator / denominator) * 100);
    }
}