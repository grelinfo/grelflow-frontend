package ch.grelinfo.grelflow.jiraclient;

import ch.grelinfo.grelflow.jiraclient.dto.SearchResults;
import ch.grelinfo.grelflow.jiraclient.exception.IssueNotFoundException;
import ch.grelinfo.grelflow.jiraclient.dto.Issue;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * TODO if Content type 'text/html' means redirect to login page, then throw exception
 */

@Component
public class JiraClient {

    private final WebClient webClient;

    public JiraClient(@Autowired JiraClientConfig config) {
        webClient = WebClient.builder()
            .baseUrl(config.url())
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Authorization", "Bearer " + config.personalAccessToken())
            .build();
    }

    public Mono<Issue> getIssue(String issueIdOrKey) {
        return webClient.get()
                .uri("/rest/api/2/issue/" + issueIdOrKey)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new IssueNotFoundException(issueIdOrKey)))
                .bodyToMono(Issue.class);
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
