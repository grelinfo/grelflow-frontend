package ch.grelinfo.grelflow.adapter.jira;

import ch.grelinfo.grelflow.adapter.jira.model.Field;
import ch.grelinfo.grelflow.adapter.jira.model.IssueType;
import ch.grelinfo.grelflow.adapter.jira.model.StatusField;
import ch.grelinfo.grelflow.adapter.jira.model.TimeTrackingField;
import ch.grelinfo.grelflow.adapter.jira.model.Issue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IssueMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String SUMMARY = "summary";
    public static final String ISSUETYPE = "issuetype";
    public static final String TIMETRACKING = "timetracking";
    public static final String STORYPOINTS = "customfield_10106";  // TODO: Make this configurable
    public static final String STATUS = "status";

    private IssueMapper() {
    }

    private static final Logger log = LoggerFactory.getLogger(IssueMapper.class);

    public static String getKey(Issue issue) { return issue.key(); }
    public static Map<String, Object> getRawFields(Issue issue) { return issue.fields(); }
    public static Optional<String> getSummary(Issue issue) {
        return extractFieldString(issue, SUMMARY);
    }
    public static Optional<String> getIssueTypeName(Issue issue) {
        return extractFieldName(issue, ISSUETYPE);
    }
    public static Optional<TimeTrackingField> getTimeTrackingField(Issue issue) {
        return extractField(issue, TIMETRACKING)
            .map(field -> mapper.convertValue(field, TimeTrackingField.class));
    }
    public static Optional<StatusField> getStatus(Issue issue) {
        return extractField(issue, STATUS)
            .map(field -> mapper.convertValue(field, StatusField.class));
    }
    public static Optional<Float> getStoryPoints(Issue issue) { return extractFieldFloat(issue, STORYPOINTS); }
    public static Optional<String> getStatusCategoryName(Issue issue) {
        return getStatus(issue).map(field -> field.statusCategory().name());
    }

    public static Issue ensureIssueType(Issue issue, String issueType) {
        getIssueTypeName(issue)
            .ifPresentOrElse(
                typeName -> {
                    if (!typeName.equals(issueType)) {
                        throw new IllegalArgumentException("Issue is not of type " + issueType);
                    }
                },
                () -> {
                    throw new IllegalArgumentException("Issue has no type");
                }
            );
        return issue;
    }
    public static Issue ensureIssueType(Issue issue, IssueType issueType) {
        return ensureIssueType(issue, issueType.value);
    }

    public static Optional<Object> extractField(Issue issue, String fieldName) {
        return Optional.ofNullable(issue.fields().get(fieldName));
    }

    public static Optional<String> extractFieldString(Issue issue, String fieldName) {
        return extractField(issue, fieldName).map(Object::toString);
    }

    public static Optional<String> extractFieldName(Issue issue, String fieldName) {
        return extractField(issue, fieldName).map(field -> mapper.convertValue(field, Field.class).name());
    }

    public static Optional<Float> extractFieldFloat(Issue issue, String fieldName) {
        return extractField(issue, fieldName).map((field -> mapper.convertValue(field, Float.class)));
    }
}
