package ch.grelinfo.grelflow.safetimetracking;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "feature-time-tracking")
public record SafeTimeTrackingConfig(
    int storyPointsToSecondsFactor,
    int allowedTimeDeviationPercentage,
    int allowedBudgetDeviationPercentage
) {}
