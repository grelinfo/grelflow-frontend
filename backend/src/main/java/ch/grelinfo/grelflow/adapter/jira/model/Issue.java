package ch.grelinfo.grelflow.adapter.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Issue(
    @NotNull String key,
    @NotNull HashMap<String, Object> fields
) {}