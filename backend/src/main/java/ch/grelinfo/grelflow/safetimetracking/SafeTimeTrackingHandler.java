package ch.grelinfo.grelflow.safetimetracking;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class SafeTimeTrackingHandler {

    private final SafeFeatureTimeTrackingRepository safeFeatureTimeTrackingRepository;

    public SafeTimeTrackingHandler(SafeFeatureTimeTrackingRepository safeFeatureTimeTrackingRepository) {
        this.safeFeatureTimeTrackingRepository = safeFeatureTimeTrackingRepository;
    }

    public Mono<ServerResponse> getFeature(ServerRequest request) {
        boolean details = request.queryParam("details").map(Boolean::parseBoolean).orElse(false);
        if (details) {
            return safeFeatureTimeTrackingRepository.findWithWorkItems(request.pathVariable("id"))
                .flatMap(featureTimeTracking -> ServerResponse.ok().bodyValue(featureTimeTracking));
        }
        return safeFeatureTimeTrackingRepository.find(request.pathVariable("id"))
            .flatMap(featureTimeTracking -> ServerResponse.ok().bodyValue(featureTimeTracking));
    }


    public Mono<ServerResponse> getFeatures(ServerRequest request) {
        boolean details = request.queryParam("details").map(Boolean::parseBoolean).orElse(false);
        Set<String> featureKeys = Set.of(request.queryParam("id").orElse("").split(","));
        if (details) {
            return safeFeatureTimeTrackingRepository.findIdsWithWorkItems(featureKeys)
                .collectList()
                .flatMap(features -> ServerResponse.ok().bodyValue(features));
        }
        return safeFeatureTimeTrackingRepository.findIds(featureKeys)
            .collectList()
            .flatMap(features -> ServerResponse.ok().bodyValue(features));
    }
}
