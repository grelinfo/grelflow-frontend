package ch.grelinfo.grelflow.safetimetracking.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrackingStatus {
    ONTRACK("ontrack"),
    UNDERSPENT("underspent"),
    OVERSPENT("overspent");

    @JsonValue
    public final String value;

    TrackingStatus(final String value) {
        this.value = value;
    }
}

