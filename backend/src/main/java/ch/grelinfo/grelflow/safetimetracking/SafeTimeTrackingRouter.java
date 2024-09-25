package ch.grelinfo.grelflow.safetimetracking;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration()
public class SafeTimeTrackingRouter {

    private static final RequestPredicate ACCEPT_JSON = accept(MediaType.APPLICATION_JSON);

    @Bean
    public RouterFunction<ServerResponse> featureTimeTrackingRoutes(SafeTimeTrackingHandler featureTimeTrackingHandler) {
        return route()
            .GET("api/v1/featureTimeTracking/{featureKey}", ACCEPT_JSON, featureTimeTrackingHandler::getFeatureTimeTracking,
                ops -> ops.beanClass(SafeFeatureTimeTrackingRepository.class).beanMethod("findFeatureTimeTracking").tag("Feature"))
            .build();
    }
}