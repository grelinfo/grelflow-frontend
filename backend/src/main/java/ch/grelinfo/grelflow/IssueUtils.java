package ch.grelinfo.grelflow;

import ch.grelinfo.grelflow.jiraclient.dto.TimeTracking;

public final class IssueUtils {

    private IssueUtils() {}

    public static TimeTracking addTimeTracking(TimeTracking timeTracking1, TimeTracking timeTracking2) {
        return new TimeTracking(
            timeTracking1.originalEstimateSeconds() + timeTracking2.originalEstimateSeconds(),
            timeTracking1.timeSpentSeconds() + timeTracking2.timeSpentSeconds(),
            timeTracking1.remainingEstimateSeconds() + timeTracking2.remainingEstimateSeconds()
        );
    }
}
