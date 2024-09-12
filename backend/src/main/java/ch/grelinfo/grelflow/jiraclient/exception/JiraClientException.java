package ch.grelinfo.grelflow.jiraclient.exception;

/**
 * Base class for Jira client exceptions.
 */
public class JiraClientException extends RuntimeException {

    public JiraClientException(String message) {
        super(message);
    }
}
