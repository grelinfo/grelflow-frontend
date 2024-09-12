package ch.grelinfo.grelflow;

import ch.grelinfo.grelflow.dto.FeatureTimeTracking;
import ch.grelinfo.grelflow.jiraclient.IssueWrapper;
import ch.grelinfo.grelflow.jiraclient.JiraClient;
import ch.grelinfo.grelflow.jiraclient.IssueType;
import ch.grelinfo.grelflow.jiraclient.dto.SearchResults;
import ch.grelinfo.grelflow.jiraclient.dto.TimeTracking;
import java.time.OffsetDateTime;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeatureTimeTrackingService {

    private final JiraClient jiraClient;

    public FeatureTimeTrackingService(@Autowired JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }
    

    public Mono<FeatureTimeTracking> getFeatureTimeTracking(String featureKey) {
        return getFeatureIssue(featureKey)
            .flatMap(issue -> computeChildIssuesTimeTracking(issue.getKey())
                .map(timeTracking -> new FeatureTimeTracking(
                    issue.getKey(),
                    OffsetDateTime.now(),
                    issue.getTimeTracking().originalEstimateSeconds(),
                    timeTracking.originalEstimateSeconds(),
                    timeTracking.timeSpentSeconds(),
                    timeTracking.remainingEstimateSeconds()
                ))
            );

    }

    private Mono<IssueWrapper> getFeatureIssue(String featureKey) {
        return jiraClient.getIssue(featureKey)
            .map(issue -> new IssueWrapper(issue).ensureIssueType(IssueType.FEATURE_SAFE));
    }

    private Flux<IssueWrapper> getChildIssuesOf(String issueKey, Set<String> fields) {
        return jiraClient.searchIssues("issuekey in childIssuesOf(" + issueKey + ")", fields)
            .map(SearchResults::issues)
            .flatMapMany(Flux::fromArray)
            .map(IssueWrapper::new);
    }

    private Mono<TimeTracking> computeChildIssuesTimeTracking(String issueKey) {
        return getChildIssuesOf(issueKey, Set.of(IssueWrapper.TIMETRACKING))
            .map(IssueWrapper::getTimeTracking)
            .reduce(
                new TimeTracking(0, 0, 0),
                IssueUtils::addTimeTracking
            );
    }
}
