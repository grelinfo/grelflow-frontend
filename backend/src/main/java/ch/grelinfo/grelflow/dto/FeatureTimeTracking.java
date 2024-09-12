package ch.grelinfo.grelflow.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record FeatureTimeTracking(
    String issueKey,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    OffsetDateTime timestamp,
    int originalEstimateSeconds,
    int computedEstimateSeconds,
    int computedTimeSpentSeconds,
    int computedRemainingEstimateSeconds
) {}
