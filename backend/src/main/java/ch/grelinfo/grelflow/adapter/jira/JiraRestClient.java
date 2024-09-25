package ch.grelinfo.grelflow.adapter.jira;

import ch.grelinfo.grelflow.adapter.jira.exception.ResourceNotFoundExceptionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * TODO if Content type 'text/html' means redirect to login page, then throw exception
 */
@Component
public class JiraRestClient {

    private final WebClient webClient;

    public JiraRestClient(JiraRestClientConfig config) {
        webClient = WebClient.builder()
            .baseUrl(config.url())
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Authorization", "Bearer " + config.personalAccessToken())
            .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public <T> Mono<T> getResource(Class<T> resourceClass, String id) {
        String apiPath = "/rest/api/2";
        return webClient.get()
            .uri(apiPath + "/" + resourceClass.getSimpleName().toLowerCase() + "/" + id)
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(ResourceNotFoundExceptionFactory.createResourceNotFoundException(resourceClass, id)))
            .bodyToMono(resourceClass);
    }




}
