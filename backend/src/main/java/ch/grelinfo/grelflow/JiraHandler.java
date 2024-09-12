package ch.grelinfo.grelflow;

import ch.grelinfo.grelflow.jiraclient.exception.IssueNotFoundException;
import ch.grelinfo.grelflow.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class JiraHandler {

    private final JiraClient jiraClient;
    private final FeatureTimeTrackingService featureTimeTrackingService;

    @Autowired
    public JiraHandler(JiraClient jiraClient, FeatureTimeTrackingService featureTimeTrackingService) {
        this.jiraClient = jiraClient;
        this.featureTimeTrackingService = featureTimeTrackingService;
    }

    public Mono<ServerResponse> getIssue(ServerRequest request) {
        return jiraClient.getIssue(request.pathVariable("issueIdorKey"))
            .flatMap(issue -> ServerResponse.ok().bodyValue(issue))
            .onErrorResume(IssueNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getFeatureTimeTracking(ServerRequest request) {
        return featureTimeTrackingService.getFeatureTimeTracking(request.pathVariable("featureKey"))
            .flatMap(featureTimeTracking -> ServerResponse.ok().bodyValue(featureTimeTracking));
    }
}
