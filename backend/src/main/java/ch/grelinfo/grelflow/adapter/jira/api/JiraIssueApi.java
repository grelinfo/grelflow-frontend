package ch.grelinfo.grelflow.adapter.jira.api;

import ch.grelinfo.grelflow.adapter.jira.JiraRestClient;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import ch.grelinfo.grelflow.adapter.jira.model.SearchResults;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class JiraIssueApi {

    private final JiraRestClient jiraRestClient;
    private final WebClient webClient;

    private static final int MAX_RESULTS = 50;

    public JiraIssueApi(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
        this.webClient = jiraRestClient.getWebClient();
    }

    public Mono<Issue> getIssue(String issueIdOrKey) {
        return jiraRestClient.getResource(Issue.class, issueIdOrKey);
    }

    public Mono<SearchResults> searchForIssues(String jql, int startAt, int maxResults, Set<String> fields) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/2/search")
                .queryParam("jql", jql)
                .queryParam("startAt", startAt)
                .queryParam("maxResults", maxResults)
                .queryParam("fields", String.join(",", fields))
                .build())
            .retrieve()
            .bodyToMono(SearchResults.class);
    }

    public Flux<Issue> searchForIssuesAsStream(String jql, Set<String> fields) {
        return searchForIssues(jql, 0, MAX_RESULTS, fields)
            .expand(searchResults -> searchResults.total() > searchResults.startAt() + searchResults.maxResults()
                ? searchForIssues(jql, searchResults.startAt() + searchResults.maxResults(), searchResults.maxResults(), fields)
                : Mono.empty())
            .flatMapIterable(SearchResults::issues);
    }
}
