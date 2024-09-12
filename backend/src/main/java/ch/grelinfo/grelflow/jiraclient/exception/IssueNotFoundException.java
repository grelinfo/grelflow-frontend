package ch.grelinfo.grelflow.jiraclient.exception;

public class IssueNotFoundException extends JiraClientException {

    public IssueNotFoundException(String issueIdorKey) {
        super("Issue " + issueIdorKey + " not found.");
    }
}
