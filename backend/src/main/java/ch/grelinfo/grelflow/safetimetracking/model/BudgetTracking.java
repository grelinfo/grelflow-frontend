package ch.grelinfo.grelflow.safetimetracking.model;

import jakarta.validation.constraints.NotNull;

public record BudgetTracking(
    @NotNull TrackingStatus status,
    @NotNull int budgetUsagePercentage,
    @NotNull int budgetDeviationPercentage,
    @NotNull int budgetSeconds,
    @NotNull int budgetRemainingSeconds
){};