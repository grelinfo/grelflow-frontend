package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkItemType {
    FEATURE("Feature"),
    STORY("Story"),
    BUG("Bug"),
    UNKNOWN("Unknown");

    @JsonValue
    public final String value;

    WorkItemType(final String value) {
        this.value = value;
    }
}
