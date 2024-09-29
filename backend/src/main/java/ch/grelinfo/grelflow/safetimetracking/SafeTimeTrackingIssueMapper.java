package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.adapter.jira.IssueMapper;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.safetimetracking.model.BudgetTracking;
import ch.grelinfo.grelflow.safetimetracking.model.Feature;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTracking;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingData;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingDataInterface;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItem;

import ch.grelinfo.grelflow.safetimetracking.model.WorkItemStatus;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItemType;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SafeTimeTrackingIssueMapper {

    private static final Map<String, WorkItemStatus> jiraIssueStatusMap = Map.of(
        "In Progress", WorkItemStatus.INPROGRESS,
        "To Do", WorkItemStatus.TODO,
        "Done", WorkItemStatus.DONE
    );

    private static final Map<String, WorkItemType> jiraIssueTypeMap = Map.of(
        "Story", WorkItemType.STORY,
        "Bug", WorkItemType.BUG,
        "Feature (SAFe)", WorkItemType.FEATURE
    );
    private final Logger log = LoggerFactory.getLogger(SafeTimeTrackingIssueMapper.class);
    private final SafeTimeTrackingConfig config;

    private SafeTimeTrackingIssueMapper(SafeTimeTrackingConfig config) {
        this.config = config;
    }

    public Feature convertToFeatureTimeTracking(Issue featureIssue, List<TimeTrackingData> workItemsTimeTrackingData) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue);
        TimeTrackingData workItemsTimeTracking = calculateTotalTimeTracking(workItemsTimeTrackingData);

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            IssueMapper.getSummary(featureIssue).orElseThrow(() -> new IllegalArgumentException("Jira issue has no summary")),
            featureStatus,
            calculateFeatureBudgetTracking(featureIssue, featureStatus, workItemsTimeTracking),
            calculateFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            null,
            warnings // TODO add more warnings
        );
    }

    public Feature convertToFeatureTimeTrackingWithWorkItems(Issue featureIssue, List<WorkItem> workItems) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue);
        TimeTrackingData workItemsTimeTracking = calculateWorkItemTotalTimeTracking(workItems);

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            IssueMapper.getSummary(featureIssue).orElseThrow(() -> new IllegalArgumentException("Jira issue has no summary")),
            featureStatus,
            calculateFeatureBudgetTracking(featureIssue, featureStatus, workItemsTimeTracking),
            calculateFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            workItems,
            warnings // TODO add more warnings
        );
    }

    public WorkItem convertToWorkItem(Issue issue) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus workItemStatus = getWorkItemStatus(issue);

        return new WorkItem(
            issue.key(),
            getWorkItemType(issue),
            workItemStatus,
            workItemTimeTrackingBuilder(issue, workItemStatus).doOnWarning(warnings::add).build(),
            warnings
        );
    }

    public TimeTrackingData convertToTimeTrackingData(Issue issue) {
        return workItemTimeTrackingBuilder(issue, getWorkItemStatus(issue)).data();
    }
    public TimeTrackingData convertToTimeTrackingData(Issue issue, WorkItemStatus workItemStatus) {
        return workItemTimeTrackingBuilder(issue, workItemStatus).data();
    }

    private TimeTrackingBuilder workItemTimeTrackingBuilder(Issue workItemIssue, WorkItemStatus workItemStatus) {

        TimeTrackingBuilder timeTrackingBuilder = new TimeTrackingBuilder()
            .setWorkItemStatus(workItemStatus)
            .setStoryPointsToSecondsFactor(config.storyPointsToSecondsFactor())
            .setAllowedTimeDeviationPercentage(config.allowedTimeDeviationPercentage());

        IssueMapper.getStoryPoints(workItemIssue).ifPresent(timeTrackingBuilder::setStoryPoints);

        IssueMapper.getTimeTrackingField(workItemIssue).ifPresent(jiraTimeTrackingField -> {
            timeTrackingBuilder
                .setPlannedTimeSeconds(jiraTimeTrackingField.originalEstimateSeconds())
                .setRemainingTimeSeconds(jiraTimeTrackingField.remainingEstimateSeconds())
                .setTimeSpentSeconds(jiraTimeTrackingField.timeSpentSeconds());
        });
        return timeTrackingBuilder;
    }

    public TimeTracking calculateFeatureTimeTracking(Issue featureIssue, WorkItemStatus featureStatus, TimeTrackingData workItemsTimeTracking) {
        TimeTrackingBuilder timeTrackingBuilder = new TimeTrackingBuilder()
            .setStoryPointsToSecondsFactor(config.storyPointsToSecondsFactor())
            .setAllowedTimeDeviationPercentage(config.allowedTimeDeviationPercentage())
            .setWorkItemStatus(featureStatus)
            .setRemainingTimeSeconds(workItemsTimeTracking.remainingTimeSeconds())
            .setTimeSpentSeconds(workItemsTimeTracking.spentTimeSeconds())
            .setPlannedTimeSeconds(workItemsTimeTracking.plannedTimeSeconds());

        IssueMapper.getStoryPoints(featureIssue).ifPresent(timeTrackingBuilder::setStoryPoints);

        return timeTrackingBuilder.build();
    }

    private BudgetTracking calculateFeatureBudgetTracking(Issue featureIssue, WorkItemStatus featureStatus, TimeTrackingData workItemsTimeTracking) {
        BudgetTrackingBuilder budgetTrackingBuilder = new BudgetTrackingBuilder()
            .setStoryPointsToSecondsFactor(config.storyPointsToSecondsFactor())
            .setAllowedBudgetDeviationPercentage(config.allowedBudgetDeviationPercentage())
            .setWorkItemStatus(featureStatus)
            .setRemainingTimeSeconds(workItemsTimeTracking.remainingTimeSeconds())
            .setTimeSpentSeconds(workItemsTimeTracking.spentTimeSeconds());

        IssueMapper.getStoryPoints(featureIssue).ifPresent(budgetTrackingBuilder::setStoryPoints);

        return budgetTrackingBuilder.build();
    }


    private WorkItemType getWorkItemType(Issue issue) {
        return IssueMapper.getIssueTypeName(issue).map(
            name -> {
                if (!jiraIssueTypeMap.containsKey(name)) {
                    log.error("Jira issue '{}' type '{}' is unknown", issue.key(), name);
                }
                return jiraIssueTypeMap.getOrDefault(name, WorkItemType.UNKNOWN);
            }
        ).orElseThrow(() -> new IllegalArgumentException(String.format("Jira issue '%s' has no type", issue.key())));
    }

    private WorkItemStatus getWorkItemStatus(Issue issue) {
        return IssueMapper.getStatusCategoryName(issue).map(
            name -> {
                if (!jiraIssueStatusMap.containsKey(name)) {
                    log.error("Jira issue '{}' status category '{}' is unknown", issue.key(), name);
                }
                return jiraIssueStatusMap.getOrDefault(name, WorkItemStatus.UNKNOWN);
            }
        ).orElseThrow(() -> new IllegalArgumentException(String.format("Jira issue '%s' has no status", issue.key())));
    }

    private static TimeTrackingData addTimeTrackingData(
        TimeTrackingDataInterface timeTrackingData1, TimeTrackingDataInterface timeTrackingData2) {
        return new TimeTrackingData(
            timeTrackingData1.plannedTimeSeconds() + timeTrackingData2.plannedTimeSeconds(),
            timeTrackingData1.spentTimeSeconds() + timeTrackingData2.spentTimeSeconds(),
            timeTrackingData1.remainingTimeSeconds() + timeTrackingData2.remainingTimeSeconds()
        );
    }

    private static TimeTrackingData calculateTotalTimeTracking(List<TimeTrackingData> timeTrackings) {
        return timeTrackings.stream().reduce(
            new TimeTrackingData(0, 0, 0),
            SafeTimeTrackingIssueMapper::addTimeTrackingData
        );
    }

    private static TimeTrackingData calculateWorkItemTotalTimeTracking(List<WorkItem> workItems) {
        return workItems.stream()
            .reduce(
                new TimeTrackingData(0, 0, 0),
                (timeTrackingData, workItem) -> addTimeTrackingData(timeTrackingData, workItem.timeTracking()),
                SafeTimeTrackingIssueMapper::addTimeTrackingData
            );
    }
}
