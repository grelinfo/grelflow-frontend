package ch.grelinfo.grelflow.adapter.jira.exception;

/**
 * Base class for Jira client exceptions.
 */
public class JiraClientException extends RuntimeException {

    public JiraClientException(String message) {
        super(message);
    }
}
