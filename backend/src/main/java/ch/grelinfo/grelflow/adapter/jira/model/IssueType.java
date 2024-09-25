package ch.grelinfo.grelflow.adapter.jira.model;

public enum IssueType {

    FEATURE_SAFE("Feature (SAFe)");

    public final String value;

    IssueType(final String value) {
        this.value = value;
    }
}
