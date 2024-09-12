package ch.grelinfo.grelflow.jiraclient;

public enum IssueType {

    FEATURE_SAFE("Feature (SAFe)");

    public final String label;

    IssueType(final String label) {
        this.label = label;
    }
}
