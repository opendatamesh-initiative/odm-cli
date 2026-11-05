package org.opendatamesh.cli.usecases.devops.starttask;

import org.opendatamesh.cli.clients.platform.devops.DevOpsActivity;
import org.opendatamesh.cli.clients.platform.devops.DevOpsActivityKey;
import org.opendatamesh.cli.clients.platform.devops.DevOpsTask;
import org.opendatamesh.cli.clients.platform.registry.RegistryDataProductVersion;
import org.opendatamesh.cli.utils.usecases.UseCase;
import org.opendatamesh.cli.utils.usecases.UseCaseException;

import java.util.Optional;

import static org.opendatamesh.cli.utils.usecases.UseCaseException.Reason.CONFLICT;

/**
 * Brings one instrumented task to RUNNING: obtain the activity, wait for its approval, request the task,
 * wait for the task's approval.
 */
class StartTask implements UseCase {

    private final StartTaskCommand command;
    private final StartTaskPresenter presenter;
    private final StartTaskDevOpsOutboundPort devOpsPort;
    private final StartTaskDescriptorOutboundPort descriptorPort;
    private final OpenActivity openActivity;
    private final VersionLookup versionLookup;
    private final ActivityPlan activityPlan;
    private final Approval approval;
    private StartTaskStep currentStep;

    StartTask(
            StartTaskCommand command,
            StartTaskPresenter presenter,
            StartTaskDevOpsOutboundPort devOpsPort,
            StartTaskRegistryOutboundPort registryPort,
            StartTaskDescriptorOutboundPort descriptorPort,
            StartTaskClockOutboundPort clockPort
    ) {
        this.command = command;
        this.presenter = presenter;
        this.devOpsPort = devOpsPort;
        this.descriptorPort = descriptorPort;
        this.openActivity = new OpenActivity(devOpsPort, clockPort, presenter, command);
        this.versionLookup = new VersionLookup(registryPort, presenter);
        this.activityPlan = new ActivityPlan();
        this.approval = new Approval(devOpsPort, clockPort, presenter, command.pollInterval());
    }

    @Override
    public void execute() {
        try {
            DevOpsActivity activity = obtainActivity();
            activity = awaitActivityApproval(activity);
            requestTask(activity);
            DevOpsTask task = awaitTaskApproval(activity);
            presenter.presentTaskStarted(activity, task);
        } catch (Exception e) {
            presenter.presentFailedStep(currentStep);
            presenter.presentException(e);
        }
    }

    private DevOpsActivity obtainActivity() {
        enter(StartTaskStep.FIND_ACTIVITY, "looking for an open activity " + command.activityName()
                + " of " + command.dataProductFqn() + " " + command.dataProductVersion());
        Optional<DevOpsActivity> found = openActivity.find(key());
        if (found.isPresent()) {
            Optional<DevOpsActivity> prepared = openActivity.prepare(found.get(), this::announceReplacement);
            if (prepared.isPresent()) {
                return prepared.get();
            }
        }
        return createActivity();
    }

    private DevOpsActivity createActivity() {
        enter(StartTaskStep.LOOK_UP_VERSION, "looking up version " + command.dataProductVersion()
                + " of " + command.dataProductFqn() + " in Registry");
        RegistryDataProductVersion version = versionLookup.publishedVersion(
                command.dataProductFqn(), command.dataProductVersion(), command.forceStart());
        enter(StartTaskStep.PLAN_ACTIVITY, "planning activity " + command.activityName() + " from the descriptor");
        DevOpsActivity plan = activityPlan.plan(descriptorPort.readLifecycle(version.content()), version.uuid(), command);
        enter(StartTaskStep.EXECUTE_ACTIVITY, "executing activity " + command.activityName() + " with " + plan.tasks().size() + " tasks");
        return switch (devOpsPort.executeActivity(plan)) {
            case ActivityCreation.Created created -> created.activity();
            case ActivityCreation.AlreadyOpen alreadyOpen -> findConcurrentActivity();
        };
    }

    private DevOpsActivity findConcurrentActivity() {
        enter(StartTaskStep.FIND_ACTIVITY, "another run opened the activity meanwhile, looking for it again");
        return openActivity.find(key()).orElseThrow(this::openActivityMissing);
    }

    private DevOpsActivity awaitActivityApproval(DevOpsActivity activity) {
        enter(StartTaskStep.AWAIT_ACTIVITY_APPROVAL, "waiting for activity " + activity.uuid() + " to be RUNNING");
        return approval.awaitActivity(activity.uuid(), command.activityApprovalTimeout());
    }

    private void requestTask(DevOpsActivity activity) {
        enter(StartTaskStep.REQUEST_TASK, "requesting task " + command.taskName() + " of activity " + activity.uuid()
                + " with provider run id " + command.providerRunId());
        devOpsPort.requestTaskExecution(key(), command.taskName(), command.providerRunId());
    }

    private DevOpsTask awaitTaskApproval(DevOpsActivity activity) {
        enter(StartTaskStep.AWAIT_TASK_APPROVAL, "waiting for task " + command.taskName() + " to be RUNNING");
        return approval.awaitTask(activity.uuid(), command.taskName(), command.taskApprovalTimeout());
    }

    private DevOpsActivityKey key() {
        return new DevOpsActivityKey(command.dataProductFqn(), command.dataProductVersion(), command.activityName());
    }

    private void announceReplacement() {
        enter(StartTaskStep.REPLACE_ACTIVITY, "replacing the open activity that blocks task " + command.taskName());
    }

    private void enter(StartTaskStep step, String detail) {
        currentStep = step;
        presenter.presentProgress(step, detail);
    }

    private UseCaseException openActivityMissing() {
        return new UseCaseException(CONFLICT, "DevOps reports an open activity " + command.activityName() + " for "
                + command.dataProductFqn() + " " + command.dataProductVersion() + ", but none was found");
    }
}
