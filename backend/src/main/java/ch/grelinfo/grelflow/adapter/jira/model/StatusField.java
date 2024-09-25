package ch.grelinfo.grelflow.adapter.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusField (
    String id,
    String name,
    Field statusCategory
) {}
