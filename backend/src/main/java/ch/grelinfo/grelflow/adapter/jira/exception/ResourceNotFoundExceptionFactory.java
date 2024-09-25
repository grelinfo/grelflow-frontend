package ch.grelinfo.grelflow.adapter.jira.exception;

import ch.grelinfo.grelflow.adapter.jira.model.Issue;

public final class ResourceNotFoundExceptionFactory {

    private ResourceNotFoundExceptionFactory() {
    }

    public static ResourceNotFoundException createResourceNotFoundException(Class<?> resourceClass, String resourceId) {
        if (resourceClass == Issue.class) {
            return new IssueNotFoundException(resourceId);
        }
        throw new IllegalArgumentException("Unknown resource class: " + resourceClass.getName());
    }

}
