package ch.grelinfo.grelflow.adapter.jira.api;

import ch.grelinfo.grelflow.adapter.jira.JiraRestClient;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.adapter.jira.model.SearchResults;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class JiraIssueApi {

    private final JiraRestClient jiraRestClient;
    private final WebClient webClient;

    public JiraIssueApi(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
        this.webClient = jiraRestClient.getWebClient();
    }

    public Mono<Issue> getIssue(String issueIdOrKey) {
        return jiraRestClient.getResource(Issue.class, issueIdOrKey);
    }

    public Mono<SearchResults> searchIssues(String jql, Set<String> fields) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/2/search")
                .queryParam("jql", jql)
                .queryParam("fields", String.join(",", fields))
                .build())
            .retrieve()
            .bodyToMono(SearchResults.class);
    }
}
