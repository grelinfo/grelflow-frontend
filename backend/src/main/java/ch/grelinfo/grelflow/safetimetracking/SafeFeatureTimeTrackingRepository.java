package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.adapter.jira.IssueMapper;
import ch.grelinfo.grelflow.adapter.jira.api.JiraIssueApi;
import ch.grelinfo.grelflow.adapter.jira.model.IssueType;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.safetimetracking.model.Feature;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingData;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItem;
import java.util.Set;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SafeFeatureTimeTrackingRepository {

    private final JiraIssueApi jiraIssueApi;
    private static final String JQL_CHILD_ISSUES = "issuekey in childIssuesOf(%s)";
    private final SafeTimeTrackingIssueMapper safeTimeTrackingIssueMapper;

    public SafeFeatureTimeTrackingRepository(JiraIssueApi jiraIssueApi,
                                            SafeTimeTrackingIssueMapper SafeTimeTrackingIssueMapper
        ) {
        this.jiraIssueApi = jiraIssueApi;
        this.safeTimeTrackingIssueMapper = SafeTimeTrackingIssueMapper;
    }
    

    public Mono<Feature> findFeatureTimeTracking(String featureId) {
        return findFeatureIssue(featureId)
            .flatMap(
                featureIssue -> findChildWorkItemsTimeTrackingData(featureId)
                    .collectList()
                    .map(childWorkItemsTimeTrackingData -> safeTimeTrackingIssueMapper.convertToFeatureTimeTracking(featureIssue, childWorkItemsTimeTrackingData))
            );
    }

    public Mono<Feature> findFeatureTimeTrackingWithWorkItems(String featureId) {
        return findFeatureIssue(featureId)
            .flatMap(
                featureIssue -> findChildWorkItems(featureId)
                    .collectList()
                    .map(childWorkItems -> safeTimeTrackingIssueMapper.convertToFeatureTimeTrackingWithWorkItems(featureIssue, childWorkItems))
            );
    }

    private Mono<Issue> findFeatureIssue(String featureId) {
        return jiraIssueApi.getIssue(featureId)
            .map(issue -> IssueMapper.ensureIssueType(issue, IssueType.FEATURE_SAFE));
    }

    public Flux<WorkItem> findChildWorkItems(String issueKey) {
        return jiraIssueApi.searchForIssuesAsStream(String.format(JQL_CHILD_ISSUES, issueKey), Set.of(IssueMapper.TIMETRACKING, IssueMapper.STORYPOINTS, IssueMapper.STATUS, IssueMapper.ISSUETYPE, IssueMapper.SUMMARY))
            .map(safeTimeTrackingIssueMapper::convertToWorkItem);
    }

    public Flux<TimeTrackingData> findChildWorkItemsTimeTrackingData(String issueKey) {
        return jiraIssueApi.searchForIssuesAsStream(String.format(JQL_CHILD_ISSUES, issueKey), Set.of(IssueMapper.TIMETRACKING, IssueMapper.STORYPOINTS, IssueMapper.STATUS))
            .map(safeTimeTrackingIssueMapper::convertToTimeTrackingData);
    }

}


