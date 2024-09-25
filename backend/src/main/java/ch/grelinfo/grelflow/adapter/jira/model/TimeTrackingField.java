package ch.grelinfo.grelflow.adapter.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TimeTrackingField(
    int originalEstimateSeconds,
    int remainingEstimateSeconds,
    int timeSpentSeconds
) {}
