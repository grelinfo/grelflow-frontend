package ch.grelinfo.grelflow.jiraclient;

import ch.grelinfo.grelflow.jiraclient.dto.TimeTracking;
import ch.grelinfo.grelflow.jiraclient.dto.Issue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class IssueWrapper {

    private final Issue issue;
    private final ObjectMapper mapper = new ObjectMapper();

    public static final String SUMMARY = "summary";
    public static final String ISSUETYPE = "issuetype";
    public static final String TIMETRACKING = "timetracking";

    public IssueWrapper(Issue issue) {
        this.issue = issue;
    }

    public final String getKey() { return issue.key(); }
    public final Map<String, Object> getRawFields() { return issue.fields(); }

    public final String getSummary() {
        return fieldToString(SUMMARY);
    }
    public final String getIssueTypeName() { return fieldToString(ISSUETYPE); }
    public final TimeTracking getTimeTracking() { return mapper.convertValue(issue.fields().get(TIMETRACKING), TimeTracking.class); }

    public IssueWrapper ensureIssueType(String issueType) {
        if (!getIssueTypeName().equals(issueType)) {
            throw new IllegalArgumentException("Issue is not a " + issueType);
        }
        return this;
    }
    public IssueWrapper ensureIssueType(IssueType issueType) {
        return ensureIssueType(issueType.label);
    }

    private String fieldToString(String fieldName) {
        Object fieldValue = issue.fields().get(fieldName);
        if (fieldValue instanceof Map) {
            return ((Map<?, ?>) fieldValue).get("name").toString();
        } else {
            return fieldValue.toString();
        }
    }
}
