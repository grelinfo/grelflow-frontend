package ch.grelinfo.grelflow.adapter.jira.model;

import java.util.List;

public record SearchResults(
    List<Issue> issues
) {}

