package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkItemStatus {
    TODO("todo"),
    INPROGRESS("inprogress"),
    DONE("done"),
    UNKNOWN("unknown");


    @JsonValue
    public final String value;

    WorkItemStatus(final String value) {
        this.value = value;
    }
}
