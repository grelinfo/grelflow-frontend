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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SafeTimeTrackingMapper {

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
    private final Logger log = LoggerFactory.getLogger(SafeTimeTrackingMapper.class);
    private final SafeTimeTrackingConfig config;

    private SafeTimeTrackingMapper(SafeTimeTrackingConfig config) {
        this.config = config;
    }

    public Feature convertToFeatureTimeTracking(Issue featureIssue, List<Issue> workItemIssues) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue, warnings::add);
        TimeTrackingData workItemsTimeTracking = calculateTotalTimeTracking(workItemIssues.stream().map(issue -> convertToTimeTrackingData(issue, featureStatus)));

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            featureStatus,
            calculateFeatureBudgetTracking(featureIssue, featureStatus, workItemsTimeTracking),
            calculateFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            null,
            warnings // TODO add more warnings
        );
    }

    public Feature convertToFeatureTimeTrackingWithWorkItems(Issue featureIssue, List<Issue> workItemIssues) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus featureStatus = getWorkItemStatus(featureIssue, warnings::add);
        List<WorkItem> workItems = workItemIssues.stream().map(this::convertToWorkItem).collect(Collectors.toList());
        TimeTrackingData workItemsTimeTracking = calculateTotalTimeTracking(workItems.stream().map(WorkItem::timeTracking));

        return new Feature(
            featureIssue.key(),
            OffsetDateTime.now(),
            featureStatus,
            calculateFeatureBudgetTracking(featureIssue, featureStatus, workItemsTimeTracking),
            calculateFeatureTimeTracking(featureIssue, featureStatus, workItemsTimeTracking),
            workItems,
            warnings // TODO add more warnings
        );
    }

    public WorkItem convertToWorkItem(Issue issue) {

        List<String> warnings = new ArrayList<>();
        WorkItemStatus workItemStatus = getWorkItemStatus(issue, warnings::add);

        return new WorkItem(
            issue.key(),
            getWorkItemType(issue, warnings::add),
            workItemStatus,
            workItemTimeTrackingBuilder(issue, workItemStatus).doOnWarning(warnings::add).build(),
            warnings
        );
    }

    private TimeTrackingData convertToTimeTrackingData(Issue issue, WorkItemStatus workItemStatus) {
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


    private static WorkItemType getWorkItemType(Issue issue, Consumer<String> warningConsumer) {
        return IssueMapper.getIssueTypeName(issue).map(
            name -> {
                if (!jiraIssueTypeMap.containsKey(name)) {
                    warningConsumer.accept(String.format("Unknown WorkItemType for Jira Issue Type '%s'", name));
                }
                return jiraIssueTypeMap.getOrDefault(name, WorkItemType.UNKNOWN);
            }
        ).orElseThrow(() -> new IllegalArgumentException("Jira issue has no type"));
    }

    private static WorkItemStatus getWorkItemStatus(Issue issue, Consumer<String> warningConsumer) {
        return IssueMapper.getStatusCategoryName(issue).map(
            name -> {
                if (!jiraIssueStatusMap.containsKey(name)) {
                    warningConsumer.accept(String.format("Unknown WorkItemStatus for Jira Status Category '%s'", name));
                }
                return jiraIssueStatusMap.getOrDefault(name, WorkItemStatus.UNKNOWN);
            }
        ).orElseThrow(() -> new IllegalArgumentException("Jira issue has no status"));
    }

    private static TimeTrackingData addTimeTrackingData(
        TimeTrackingDataInterface timeTrackingData1, TimeTrackingDataInterface timeTrackingData2) {
        return new TimeTrackingData(
            timeTrackingData1.plannedTimeSeconds() + timeTrackingData2.plannedTimeSeconds(),
            timeTrackingData1.spentTimeSeconds() + timeTrackingData2.spentTimeSeconds(),
            timeTrackingData1.remainingTimeSeconds() + timeTrackingData2.remainingTimeSeconds()
        );
    }

    private static TimeTrackingData calculateTotalTimeTracking(Stream<TimeTrackingDataInterface> timeTrackings) {
        return (TimeTrackingData) timeTrackings.reduce(
            new TimeTrackingData(0, 0, 0),
            SafeTimeTrackingMapper::addTimeTrackingData
        );
    }
}
