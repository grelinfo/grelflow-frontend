package ch.grelinfo.grelflow.safetimetracking.model;

/**
 * Interface for time tracking data.
 */
public interface TimeTrackingDataInterface {
    /**
     * Returns the original estimated time in seconds.
     * This value does not change as time passes and is based on the original estimation.
     *
     * @return The original estimated time in seconds.
     */
    int originalEstimateSeconds();

    /**
     * Returns the remaining estimated time in seconds.
     * This value change as time passes according to estimation of remaining work.
     *
     * @return The remaining estimated time in seconds.
     */
    int remainingEstimateSeconds();

    /**
     * Returns the spent time in seconds.
     * This value change as time passes according to the work done.
     *
     * @return The spent time in seconds.
     */
    int timeSpentSeconds();
}