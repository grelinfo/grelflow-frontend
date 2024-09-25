package ch.grelinfo.grelflow.adapter.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Field(
    String id,
    String name
) {}
