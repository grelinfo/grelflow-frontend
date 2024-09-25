package ch.grelinfo.grelflow.safetimetracking;

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

    public Mono<ServerResponse> getFeatureTimeTracking(ServerRequest request) {
        boolean details = request.queryParam("details").map(Boolean::parseBoolean).orElse(false);
        if (details) {
            return safeFeatureTimeTrackingRepository.findFeatureTimeTrackingWithWorkItems(request.pathVariable("featureKey"))
                .flatMap(featureTimeTracking -> ServerResponse.ok().bodyValue(featureTimeTracking));
        }
        return safeFeatureTimeTrackingRepository.findFeatureTimeTracking(request.pathVariable("featureKey"))
            .flatMap(featureTimeTracking -> ServerResponse.ok().bodyValue(featureTimeTracking));
    }
}
