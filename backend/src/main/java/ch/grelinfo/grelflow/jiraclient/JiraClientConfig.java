package ch.grelinfo.grelflow.jiraclient;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "jira")
public record JiraClientConfig(
    String url,
    String personalAccessToken
) {}