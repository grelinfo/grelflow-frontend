package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.adapter.jira.IssueMapper;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
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

    public Feature convertToFeatureTimeTracking(Issue featureIssue, Stream<TimeTrackingData> workItemsTimeTrackingData) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue);
        TimeTrackingData workItemsTimeTracking = calculateTotalTimeTracking(workItemsTimeTrackingData);

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            IssueMapper.getSummary(featureIssue).orElseThrow(() -> new IllegalArgumentException("Jira issue has no summary")),
            featureStatus,
            mapFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            null,
            warnings // TODO add more warnings
        );
    }

    public Feature convertToFeatureTimeTrackingWithWorkItems(Issue featureIssue, Stream<WorkItem> workItems) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue);
        List<WorkItem> workItemsList = workItems.toList();
        TimeTrackingData workItemsTimeTracking = calculateWorkItemTotalTimeTracking(workItemsList);

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            IssueMapper.getSummary(featureIssue).orElseThrow(() -> new IllegalArgumentException("Jira issue has no summary")),
            featureStatus,
            mapFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            workItemsList,
            warnings // TODO add more warnings
        );
    }

    public WorkItem convertToWorkItem(Issue issue) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus workItemStatus = getWorkItemStatus(issue);
        WorkItemType workItemType = getWorkItemType(issue);

        return new WorkItem(
            issue.key(),
            workItemType,
            workItemStatus,
            workItemTimeTrackingBuilder(issue, workItemStatus, workItemType).doOnWarning(warnings::add).build(),
            warnings
        );
    }

    public TimeTrackingData convertToTimeTrackingData(Issue issue) {
        return workItemTimeTrackingBuilder(issue, getWorkItemStatus(issue), getWorkItemType(issue)).data();
    }

    private TimeTrackingBuilder workItemTimeTrackingBuilder(Issue workItemIssue, WorkItemStatus workItemStatus, WorkItemType workItemType) {

        TimeTrackingBuilder timeTrackingBuilder = new TimeTrackingBuilder()
            .setWorkItemType(workItemType)
            .setWorkItemStatus(workItemStatus)
            .setStoryPointsToSecondsFactor(config.storyPointsToSecondsFactor())
            .setAllowedTimeDeviationPercentage(config.allowedTimeDeviationPercentage());

        IssueMapper.getStoryPoints(workItemIssue).ifPresent(timeTrackingBuilder::setStoryPoints);

        IssueMapper.getTimeTrackingField(workItemIssue).ifPresent(jiraTimeTrackingField -> {
            timeTrackingBuilder
                .setOriginalEstimateSeconds(jiraTimeTrackingField.originalEstimateSeconds())
                .setRemainingEstimateSeconds(jiraTimeTrackingField.remainingEstimateSeconds())
                .setTimeSpentSeconds(jiraTimeTrackingField.timeSpentSeconds());
        });
        return timeTrackingBuilder;
    }


    private TimeTracking mapFeatureTimeTracking(Issue featureIssue, WorkItemStatus featureStatus, TimeTrackingData workItemsTimeTracking) {
        TimeTrackingBuilder timeTrackingBuilder = new TimeTrackingBuilder()
            .setWorkItemType(WorkItemType.FEATURE)
            .setStoryPointsToSecondsFactor(config.storyPointsToSecondsFactor())
            .setAllowedTimeDeviationPercentage(config.allowedTimeDeviationPercentage())
            .setWorkItemStatus(featureStatus)
            .setRemainingEstimateSeconds(workItemsTimeTracking.remainingEstimateSeconds())
            .setTimeSpentSeconds(workItemsTimeTracking.timeSpentSeconds());

        IssueMapper.getStoryPoints(featureIssue).ifPresent(timeTrackingBuilder::setStoryPoints);

        return timeTrackingBuilder.build();
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
            timeTrackingData1.originalEstimateSeconds() + timeTrackingData2.originalEstimateSeconds(),
            timeTrackingData1.remainingEstimateSeconds() + timeTrackingData2.remainingEstimateSeconds(),
            timeTrackingData1.timeSpentSeconds() + timeTrackingData2.timeSpentSeconds()
        );
    }

    private static TimeTrackingData calculateTotalTimeTracking(Stream<TimeTrackingData> timeTrackings) {
        return timeTrackings.reduce(
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
