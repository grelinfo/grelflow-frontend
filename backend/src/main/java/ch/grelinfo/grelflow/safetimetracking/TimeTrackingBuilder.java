package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.safetimetracking.model.TimeTracking;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingData;
import ch.grelinfo.grelflow.safetimetracking.model.TrackingStatus;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItemStatus;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItemType;
import java.util.List;
import java.util.function.Consumer;

public class TimeTrackingBuilder {


    private static final List<Float> STORY_POINT_FIBONACCI_SERIES = List.of(0.0f, 0.5f, 1.0f, 2.0f, 3.0f, 5.0f, 8.0f, 13.0f, 20.0f, 40.0f, 100.0f);

    private WorkItemType workItemType;
    private Integer storyPointsToSecondsFactor;
    private Integer allowedTimeDeviationPercentage;
    private WorkItemStatus workItemStatus;
    private Float storyPoints;
    private Integer plannedTimeSeconds;
    private Integer remainingEstimateSeconds;
    private Integer timeSpentSeconds;
    private Consumer<String> warningConsumer;

    public TimeTrackingBuilder setWorkItemType(WorkItemType workItemType) {
        this.workItemType = mapIfEmptyOrThrow(this.workItemType, workItemType, "workItemType");
        return this;
    }

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

    public TimeTrackingBuilder setRemainingEstimateSeconds(int remainingEstimateSeconds) {
        this.remainingEstimateSeconds = mapIfEmptyOrThrow(this.remainingEstimateSeconds, remainingEstimateSeconds, "remainingEstimateSeconds");
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
        int originalEstimateTimeSeconds = getOriginalEstimateTimeSeconds(isTimeTrackingPresent);
        int timeSpentSeconds = getTimeSpentSeconds();
        int remainingEstimateTimeSeconds = getRemainingEstimateTimeSeconds(originalEstimateTimeSeconds, timeSpentSeconds, isTimeTrackingPresent);
        int forecastedTimeSpentSeconds = getEstimatedTimeSpent(remainingEstimateTimeSeconds, timeSpentSeconds);

        warnIfPlannedTimeFarFromStoryPoint();
        warnIfEitherStoryPointsOrOriginalEstimateIsMissing();
        warnIfTimeTrackingIsMissing(isTimeTrackingPresent);

        return new TimeTracking(
            getStatus(timeSpentSeconds, originalEstimateTimeSeconds),
            getUsagePercentage(timeSpentSeconds, originalEstimateTimeSeconds),
            originalEstimateTimeSeconds,
            remainingEstimateTimeSeconds,
            timeSpentSeconds,
            getEstimatedStatus(workItemStatus, forecastedTimeSpentSeconds, originalEstimateTimeSeconds),
            getForcastedCompletionPercentage(forecastedTimeSpentSeconds, timeSpentSeconds),
            getEstimatedUsagePercentage(forecastedTimeSpentSeconds, originalEstimateTimeSeconds)
        );
    }

    public TimeTrackingData data() {
        boolean isTimeTrackingPresent = isTimeTrackingPresent();
        int plannedTimeSeconds = getOriginalEstimateTimeSeconds(isTimeTrackingPresent);
        int timeSpentSeconds = getTimeSpentSeconds();

        return new TimeTrackingData(
            plannedTimeSeconds,
            timeSpentSeconds,
            getRemainingEstimateTimeSeconds(plannedTimeSeconds, timeSpentSeconds, isTimeTrackingPresent)
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

    private float calculateTimeSecondsToStoryPoints(int timeSeconds) {
        if (storyPointsToSecondsFactor == null || timeSeconds == 0 || storyPointsToSecondsFactor == 0) {
            return 0;
        }
        return (float) ((double) timeSeconds / storyPointsToSecondsFactor);
    }

    private static float nextSmallerFibonacci(float number) {
        return STORY_POINT_FIBONACCI_SERIES.stream().filter(fibonacci -> fibonacci < number).reduce((first, second) -> second).orElse(number);
    }

    private static float nextBiggerFibonacci(float number) {
        return STORY_POINT_FIBONACCI_SERIES.stream().filter(fibonacci -> fibonacci > number).findFirst().orElse(number);
    }

    private <T> T mapIfEmptyOrThrow(T field, T value, String name) {
        if (field != null) {
            throw new IllegalStateException(name + " already set.");
        }
        return value;
    }

    private Integer getForcastedCompletionPercentage(int forecastedTimeSpentSeconds, int timeSpentSeconds) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return null;
        }
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.TODO)) {
            return 0;
        }
        if (forecastedTimeSpentSeconds == 0) {
            return 0;
        }
        return (int) (((double) timeSpentSeconds / forecastedTimeSpentSeconds) * 100);
    }

    private int getOriginalEstimateTimeSeconds(boolean isTimeTrackingPresent) {
        if (workItemType != null && workItemType.equals(WorkItemType.FEATURE)) {
            return calculateStoryPointSeconds();
        }
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
       (remainingEstimateSeconds != null && remainingEstimateSeconds != 0) ||
       (timeSpentSeconds != null && timeSpentSeconds != 0);
    }

    private TrackingStatus getStatus(int timeSpentSeconds, int originalEstimateTimeSeconds) {
        if (workItemType != null && workItemType.equals(WorkItemType.FEATURE)) {
            return getTimeBasedStatus(timeSpentSeconds, originalEstimateTimeSeconds);
        }
        return getStoryPointsBasedStatus(timeSpentSeconds, originalEstimateTimeSeconds);
    }

    private TrackingStatus getEstimatedStatus(WorkItemStatus workItemStatus, int forecastedTimeSpentSeconds, int originalEstimateTimeSeconds) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return null;
        }
        if (workItemType != null && workItemType.equals(WorkItemType.FEATURE)) {
            return getTimeBasedStatus(forecastedTimeSpentSeconds, originalEstimateTimeSeconds);
        }
        return getStoryPointsBasedStatus(forecastedTimeSpentSeconds, originalEstimateTimeSeconds);
    }

    private Integer getEstimatedUsagePercentage(int forecastedTimeSpentSeconds, int plannedTimeSeconds) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return null;
        }
        return calculatePercentage(forecastedTimeSpentSeconds, plannedTimeSeconds);
    }

    private static int getEstimatedTimeSpent(int remainingEstimateTimeSeconds, int timeSpentSeconds) {
        return timeSpentSeconds + remainingEstimateTimeSeconds;
    }

    private TrackingStatus getTimeBasedStatus(int effectiveTimeSeconds, int originalTimeSeconds) {
        int timeDeviationPercentage = calculateTimeDeviationPercentage(effectiveTimeSeconds, originalTimeSeconds);
        int allowedTimeDeviationPercentage = this.allowedTimeDeviationPercentage != null ? this.allowedTimeDeviationPercentage : 0;
        if (timeDeviationPercentage > allowedTimeDeviationPercentage) {
            return TrackingStatus.OVERSPENT;
        }
        if (timeDeviationPercentage < -allowedTimeDeviationPercentage) {
            return TrackingStatus.UNDERSPENT;
        }
        return TrackingStatus.ONTRACK;
    }

    private TrackingStatus getStoryPointsBasedStatus(int effectiveTimeSeconds, int originalTimeSeconds) {
        float originalStoryPoints = calculateTimeSecondsToStoryPoints(originalTimeSeconds);
        float effectiveStoryPoints = calculateTimeSecondsToStoryPoints(effectiveTimeSeconds);
        return getStoryPointsBasedStatus(originalStoryPoints, effectiveStoryPoints);
    }
    private TrackingStatus getStoryPointsBasedStatus(float originalStoryPoints, float effectiveStoryPoints) {
        float largerFibonacci = nextBiggerFibonacci(effectiveStoryPoints);
        float smallerFibonacci = nextSmallerFibonacci(effectiveStoryPoints);
        if (largerFibonacci > originalStoryPoints) {
            return TrackingStatus.OVERSPENT;
        }
        if (smallerFibonacci < originalStoryPoints) {
            return TrackingStatus.UNDERSPENT;
        }
        return TrackingStatus.ONTRACK;
    }

    private int getRemainingEstimateTimeSeconds(int plannedTimeSeconds, int timeSpentSeconds, boolean isTimeTrackingPresent) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return 0;
        }
        return remainingEstimateSeconds != null && isTimeTrackingPresent ? remainingEstimateSeconds : (plannedTimeSeconds - timeSpentSeconds);
    }

    private int getUsagePercentage(int timeSpentSeconds, int plannedTimeSeconds) {
        return calculatePercentage(timeSpentSeconds, plannedTimeSeconds);
    }



    /**
     * Calculate the time deviation percentage.
     *
     * @param originalTimeSeconds the original time in seconds
     * @param effectiveTimeSeconds then effective time in seconds
     * @return the deviation percentage
     */
    private static int calculateTimeDeviationPercentage(int effectiveTimeSeconds, int originalTimeSeconds) {
        return calculatePercentage(effectiveTimeSeconds, originalTimeSeconds) - 100;
    }

    private static int calculatePercentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }
        return (int) (((double) numerator / denominator) * 100);
    }
}