package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record WorkItem(
    @NotNull String id,
    @NotNull WorkItemType type,
    @NotNull WorkItemStatus status,
    @NotNull TimeTracking timeTracking,
    @JsonInclude(value= Include.NON_EMPTY, content=Include.NON_NULL)
    List<String> warnings
) {}