package ch.grelinfo.grelflow.adapter.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "jira")
public record JiraRestClientConfig(
    String url,
    String personalAccessToken
) {}