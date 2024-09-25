package ch.grelinfo.grelflow.adapter.jira.exception;


public abstract class ResourceNotFoundException extends RuntimeException {

    /**
     * The name of the resource that was not found.
     * This should be overridden by subclasses to provide a more specific name.
     */
    public static final String resourceName = "Resource";
    public final String resourceId;

    protected ResourceNotFoundException(String resourceId) {
        super(String.format("%s with ID '%s' not found", resourceName, resourceId));
        this.resourceId = resourceId;
    }
}
