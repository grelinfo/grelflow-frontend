package ch.grelinfo.grelflow.safetimetracking;

import ch.grelinfo.grelflow.adapter.jira.IssueMapper;
import ch.grelinfo.grelflow.adapter.jira.api.JiraIssueApi;
import ch.grelinfo.grelflow.adapter.jira.model.IssueType;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.adapter.jira.model.SearchResults;
import ch.grelinfo.grelflow.safetimetracking.model.Feature;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class SafeFeatureTimeTrackingRepository {

    private final JiraIssueApi jiraIssueApi;
    private static final String JQL_CHILD_ISSUES = "issuekey in childIssuesOf(%s)";
    private final SafeTimeTrackingMapper safeTimeTrackingMapper;

    public SafeFeatureTimeTrackingRepository(JiraIssueApi jiraIssueApi,
                                            SafeTimeTrackingMapper SafeTimeTrackingMapper
        ) {
        this.jiraIssueApi = jiraIssueApi;
        this.safeTimeTrackingMapper = SafeTimeTrackingMapper;
    }
    

    public Mono<Feature> findFeatureTimeTracking(String featureId) {
        return findFeatureIssue(featureId)
            .flatMap(issue -> findWorkItemIssues(featureId)
                .map(childIssues -> safeTimeTrackingMapper.convertToFeatureTimeTracking(issue, childIssues))
            );
    }

    public Mono<Feature> findFeatureTimeTrackingWithWorkItems(String featureId) {
        return findFeatureIssue(featureId)
            .flatMap(issue -> findWorkItemIssues(featureId)
                .map(childIssues -> safeTimeTrackingMapper.convertToFeatureTimeTrackingWithWorkItems(issue, childIssues))
            );
    }

    private Mono<Issue> findFeatureIssue(String featureId) {
        return jiraIssueApi.getIssue(featureId)
            .map(issue -> IssueMapper.ensureIssueType(issue, IssueType.FEATURE_SAFE));
    }

    private Mono<List<Issue>> findWorkItemIssues(String featureKey) {
        return jiraIssueApi.searchIssues(String.format(JQL_CHILD_ISSUES, featureKey), Set.of(IssueMapper.ISSUETYPE, IssueMapper.TIMETRACKING, IssueMapper.STORYPOINTS, IssueMapper.STATUS, IssueMapper.SUMMARY))
            .map(SearchResults::issues);
    }

}


