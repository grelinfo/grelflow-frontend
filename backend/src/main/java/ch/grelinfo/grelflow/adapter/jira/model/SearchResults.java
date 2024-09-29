package ch.grelinfo.grelflow.adapter.jira.model;

import java.util.List;

public record SearchResults(
    int startAt,
    int maxResults,
    int total,
    List<Issue> issues
) {}

