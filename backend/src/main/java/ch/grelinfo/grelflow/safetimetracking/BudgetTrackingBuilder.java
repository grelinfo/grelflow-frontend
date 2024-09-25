package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.safetimetracking.model.BudgetTracking;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTracking;
import ch.grelinfo.grelflow.safetimetracking.model.TrackingStatus;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItemStatus;
import java.util.function.Consumer;

public class BudgetTrackingBuilder {

    private Integer storyPointsToSecondsFactor;
    private Integer allowedBudgetDeviationPercentage;
    private WorkItemStatus workItemStatus;
    private Float storyPoints;
    private Integer budgetSeconds;
    private Integer timeSpentSeconds;
    private Integer remainingTimeSeconds;
    private Consumer<String> warningConsumer;

    public BudgetTrackingBuilder setStoryPointsToSecondsFactor(int storyPointsToSecondsFactor) {
        this.storyPointsToSecondsFactor = mapIfEmptyOrThrow(this.storyPointsToSecondsFactor, storyPointsToSecondsFactor, "storyPointsToSecondsFactor");
        return this;
    }

    public BudgetTrackingBuilder setAllowedBudgetDeviationPercentage(int allowedBudgetDeviationPercentage) {
        this.allowedBudgetDeviationPercentage = mapIfEmptyOrThrow(this.allowedBudgetDeviationPercentage, allowedBudgetDeviationPercentage, "allowedBudgetDeviationPercentage");
        return this;
    }

    public BudgetTrackingBuilder setStoryPoints(float storyPoints) {
        this.storyPoints = mapIfEmptyOrThrow(this.storyPoints, storyPoints, "storyPoints");
        return this;
    }

    public BudgetTrackingBuilder setBudgetSeconds(int budgetSeconds) {
        this.budgetSeconds = mapIfEmptyOrThrow(this.budgetSeconds, budgetSeconds, "budgetSeconds");
        return this;
    }

    public BudgetTrackingBuilder setRemainingTimeSeconds(int remainingTimeSeconds) {
        this.remainingTimeSeconds = mapIfEmptyOrThrow(this.remainingTimeSeconds, remainingTimeSeconds, "remainingTimeSeconds");
        return this;
    }

    public BudgetTrackingBuilder setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = mapIfEmptyOrThrow(this.timeSpentSeconds, timeSpentSeconds, "timeSpentSeconds");
        return this;
    }

    public BudgetTrackingBuilder setWorkItemStatus(WorkItemStatus workItemStatus) {
        this.workItemStatus = mapIfEmptyOrThrow(this.workItemStatus, workItemStatus, "workItemStatus");
        return this;
    }

    public BudgetTrackingBuilder doOnWarning(Consumer<String> consumer) {
        this.warningConsumer = mapIfEmptyOrThrow(this.warningConsumer, consumer, "warningConsumer");
        return this;
    }

    public BudgetTracking build() {
        int plannedTimeSeconds = getPlannedTimeSeconds();
        int timeSpentSeconds = getTimeSpentSeconds();
        int remainingTimeSeconds = calculateRemainingTimeSeconds(plannedTimeSeconds, timeSpentSeconds);
        int plannedTimeUsageSeconds = calculatePlannedTimeUsageSeconds(remainingTimeSeconds, timeSpentSeconds);
        int budgetSeconds = getBudgetSeconds();
        int budgetDeviationPercentage = calculateBudgetDeviationPercentage(budgetSeconds, plannedTimeUsageSeconds);

        return new BudgetTracking(
            getStatus(budgetDeviationPercentage),
            calculateBudgetUsagePercentage(plannedTimeSeconds, plannedTimeUsageSeconds),
            budgetDeviationPercentage,
            budgetSeconds,
            calculateBudgetRemainingSeconds(budgetSeconds, timeSpentSeconds)
        );
    }

    private int getBudgetSeconds() {
        return budgetSeconds != null ? budgetSeconds : calculateStoryPointSeconds();
    }

    private int calculateStoryPointSeconds() {
        if (storyPointsToSecondsFactor == null || storyPoints == null) {
            return 0;
        }
        return (int) (storyPoints * storyPointsToSecondsFactor);
    }

    private <T> T mapIfEmptyOrThrow(T field, T value, String name) {
        if (field != null) {
            throw new IllegalStateException(name + " already set.");
        }
        return value;
    }

    private int getPlannedTimeSeconds() {
        return budgetSeconds != null ? budgetSeconds : calculateStoryPointSeconds();
    }

    private int getTimeSpentSeconds() {
        return timeSpentSeconds != null ? timeSpentSeconds : 0;
    }


    private TrackingStatus getStatus(int budgetDeviationPercentage) {
        int allowedTimeDeviationPercentage = this.allowedBudgetDeviationPercentage != null ? this.allowedBudgetDeviationPercentage : 0;
        if (budgetDeviationPercentage > allowedTimeDeviationPercentage) {
            return TrackingStatus.OVERSPENT;
        }
        if (budgetDeviationPercentage < -allowedTimeDeviationPercentage) {
            return TrackingStatus.UNDERSPENT;
        }
        return TrackingStatus.ONTRACK;
    }

    private int calculateRemainingTimeSeconds(int plannedTimeSeconds, int timeSpentSeconds) {
        if (workItemStatus != null && workItemStatus.equals(WorkItemStatus.DONE)) {
            return 0;
        }
        return remainingTimeSeconds != null ? remainingTimeSeconds : (plannedTimeSeconds - timeSpentSeconds);
    }

    private static int calculateBudgetUsagePercentage(int budgetSeconds, int plannedTimeUsageSeconds) {
        return calculatePercentage(plannedTimeUsageSeconds, budgetSeconds);
    }

    private static int calculatePlannedTimeUsageSeconds(int remainingTimeSeconds, int timeSpentSeconds) {
        return timeSpentSeconds + remainingTimeSeconds;
    }

    private static int calculateBudgetRemainingSeconds(int budgetSeconds, int timeSpentSeconds) {
        return budgetSeconds - timeSpentSeconds;
    }

    /**
     * Calculate the deviation percentage of the budget usage.
     *
     * @param budgetSeconds the budget in seconds
     * @param plannedTimeUsageSeconds the planned time usage in seconds
     * @return the deviation percentage
     */
    private static int calculateBudgetDeviationPercentage(int budgetSeconds, int plannedTimeUsageSeconds) {
        return calculatePercentage(plannedTimeUsageSeconds, budgetSeconds) - 100;
    }

    private static int calculatePercentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }
        return (int) (((double) numerator / denominator) * 100);
    }
}