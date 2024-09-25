package ch.grelinfo.grelflow.safetimetracking.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;


public record Feature(
    @NotNull String id,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @NotNull OffsetDateTime recordedTimestamp,
    @NotNull WorkItemStatus status,
    @NotNull BudgetTracking budgetTracking,
    @NotNull TimeTracking timeTracking,
    @JsonInclude(value= Include.NON_EMPTY, content=Include.NON_NULL)
    List<WorkItem> workItems,
    @JsonInclude(value= Include.NON_EMPTY, content=Include.NON_NULL)
    List<String> warnings
) {}
