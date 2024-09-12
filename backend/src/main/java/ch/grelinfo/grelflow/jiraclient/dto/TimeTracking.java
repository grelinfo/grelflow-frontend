package ch.grelinfo.grelflow.jiraclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeTracking(
    int originalEstimateSeconds,
    int remainingEstimateSeconds,
    int timeSpentSeconds
) {}
