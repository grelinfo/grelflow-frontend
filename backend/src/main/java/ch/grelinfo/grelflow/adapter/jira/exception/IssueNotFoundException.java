package ch.grelinfo.grelflow.adapter.jira.exception;


public class IssueNotFoundException extends ResourceNotFoundException {

    final static String resourceName = "Issue";

    protected IssueNotFoundException(String resourceId) {
        super(resourceId);
    }
}
