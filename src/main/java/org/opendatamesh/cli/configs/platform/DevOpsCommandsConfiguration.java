package org.opendatamesh.cli.configs.platform;

import java.time.Duration;

/**
 * Timings of the {@code odm-cli platform devops} commands, bound from {@code cli.platform.devops}.
 * The defaults are safe everywhere, so a missing block still works.
 */
public class DevOpsCommandsConfiguration {
    private Duration pollInterval = Duration.ofSeconds(5);
    private Duration activityApprovalTimeout = Duration.ofMinutes(30);
    private Duration taskApprovalTimeout = Duration.ofMinutes(10);
    private int runIdLookupAttempts = 5;
    private Duration runIdLookupInterval = Duration.ofSeconds(2);
    private int conflictRetries = 3;
    private Duration conflictRetryInterval = Duration.ofSeconds(1);

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Duration getActivityApprovalTimeout() {
        return activityApprovalTimeout;
    }

    public void setActivityApprovalTimeout(Duration activityApprovalTimeout) {
        this.activityApprovalTimeout = activityApprovalTimeout;
    }

    public Duration getTaskApprovalTimeout() {
        return taskApprovalTimeout;
    }

    public void setTaskApprovalTimeout(Duration taskApprovalTimeout) {
        this.taskApprovalTimeout = taskApprovalTimeout;
    }

    public int getRunIdLookupAttempts() {
        return runIdLookupAttempts;
    }

    public void setRunIdLookupAttempts(int runIdLookupAttempts) {
        this.runIdLookupAttempts = runIdLookupAttempts;
    }

    public Duration getRunIdLookupInterval() {
        return runIdLookupInterval;
    }

    public void setRunIdLookupInterval(Duration runIdLookupInterval) {
        this.runIdLookupInterval = runIdLookupInterval;
    }

    public int getConflictRetries() {
        return conflictRetries;
    }

    public void setConflictRetries(int conflictRetries) {
        this.conflictRetries = conflictRetries;
    }

    public Duration getConflictRetryInterval() {
        return conflictRetryInterval;
    }

    public void setConflictRetryInterval(Duration conflictRetryInterval) {
        this.conflictRetryInterval = conflictRetryInterval;
    }
}
