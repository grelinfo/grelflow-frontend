package ch.grelinfo.grelflow;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class RoutingConfiguration {

    private static final RequestPredicate ACCEPT_JSON = accept(MediaType.APPLICATION_JSON);

    @Bean
    public RouterFunction<ServerResponse> monoRouterFunction(JiraHandler jiraHandler) {
        return route()
            .GET("/issue/{issueIdorKey}", ACCEPT_JSON, jiraHandler::getIssue)
            .GET("/featureTimeTracking/{featureKey}", ACCEPT_JSON, jiraHandler::getFeatureTimeTracking)
            .build();
    }
}