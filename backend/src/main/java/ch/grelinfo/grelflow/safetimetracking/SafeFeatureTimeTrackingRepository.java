package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.adapter.jira.IssueMapper;
import ch.grelinfo.grelflow.adapter.jira.api.JiraIssueApi;
import ch.grelinfo.grelflow.adapter.jira.model.IssueType;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.safetimetracking.model.Feature;
import ch.grelinfo.grelflow.safetimetracking.model.TimeTrackingData;
import ch.grelinfo.grelflow.safetimetracking.model.WorkItem;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    public Mono<Feature> find(String featureId) {
        return findFeatureIssue(featureId)
            .publishOn(Schedulers.boundedElastic())
            .map(featureIssue -> {
                Stream<TimeTrackingData> workItemsTimeTrackingData = findChildWorkItemsTimeTrackingData(featureId).toStream();
                return safeTimeTrackingIssueMapper.convertToFeatureTimeTracking(featureIssue, workItemsTimeTrackingData);
            });
    }

    public Flux<Feature> findIds(Set<String> featureIds) {
        return Flux.fromIterable(featureIds)
            .flatMap(this::find);
    }

    public Mono<Feature> findWithWorkItems(String featureId) {
        return findFeatureIssue(featureId)
            .publishOn(Schedulers.boundedElastic())
            .map(featureIssue -> {
                Stream <WorkItem> workItems = findChildWorkItems(featureId).toStream();
                return safeTimeTrackingIssueMapper.convertToFeatureTimeTrackingWithWorkItems(featureIssue, workItems);
            });
    }

    public Flux<Feature> findIdsWithWorkItems(Set<String> featureIds) {
        return Flux.fromIterable(featureIds)
            .flatMap(this::findWithWorkItems);
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
        return jiraIssueApi.searchForIssuesAsStream(String.format(JQL_CHILD_ISSUES, issueKey), Set.of(IssueMapper.TIMETRACKING, IssueMapper.STORYPOINTS, IssueMapper.STATUS, IssueMapper.ISSUETYPE))
            .map(safeTimeTrackingIssueMapper::convertToTimeTrackingData);
    }

}


