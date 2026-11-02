package org.opendatamesh.cli.commands.platform.devops.starttask;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.commands.platform.devops.DurationConverter;
import org.opendatamesh.cli.commands.platform.devops.ResolvedValueConverter;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.devops.starttask.StartTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.platform.devops.DevOpsCommandBuilder.DEVOPS_COMMAND;

@Component
public class StartTaskCommandBuilder implements PicoCliCommandBuilder {
    public static final String DEVOPS_START_TASK_COMMAND = "start-task";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Autowired
    private StartTaskFactory startTaskFactory;

    @Override
    public CommandLine buildCommand(String... args) {
        StartTaskCommandExecutor executor = new StartTaskCommandExecutor(odmCliConfiguration, startTaskFactory);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(DEVOPS_START_TASK_COMMAND);
        spec.usageMessage().description("Command to bring one instrumented task of a DevOps activity to RUNNING: it finds or creates the activity, "
                + "waits for the policy approvals and requests the task with the pipeline's run id.");
        spec.mixinStandardHelpOptions(true);

        handleWithOrder(Lists.newArrayList(
                order -> handleRequiredParam(spec, order, "--data-product-fqn", "FQN",
                        "Fully qualified name of the data product.", executor::setDataProductFqn),
                order -> handleRequiredParam(spec, order, "--data-product-version", "VERSION",
                        "Version of the data product. It must equal the descriptor's info.version (the Registry versionNumber), not the git tag.",
                        executor::setDataProductVersion),
                order -> handleRequiredParam(spec, order, "--activity", "STAGE",
                        "Name of the activity: the stage of the descriptor's lifecycleInfo.", executor::setActivityName),
                order -> handleRequiredParam(spec, order, "--task", "TASK",
                        "Name of the task to start.", executor::setTaskName),
                order -> handleRequiredParam(spec, order, "--provider-run-id", "RUN_ID",
                        "Run id of the pipeline, for example the CI pipeline id. complete-task must be given the same value.",
                        executor::setProviderRunId),
                order -> handleForceStartParam(spec, order, executor),
                order -> handleDurationParam(spec, order, "--poll-interval", "Interval between two polls while waiting for an approval, at least 1s (default from cli.platform.devops.pollInterval).", Duration.ofSeconds(1), executor::setPollInterval),
                order -> handleDurationParam(spec, order, "--activity-timeout", "Maximum time to wait for the activity approval (default from cli.platform.devops.activityApprovalTimeout).", Duration.ZERO, executor::setActivityApprovalTimeout),
                order -> handleDurationParam(spec, order, "--task-timeout", "Maximum time to wait for the task approval (default from cli.platform.devops.taskApprovalTimeout).", Duration.ZERO, executor::setTaskApprovalTimeout)
        ));

        commands.stream().filter(command -> DEVOPS_START_TASK_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    private void handleRequiredParam(CommandLine.Model.CommandSpec spec, int order, String name, String label,
                                     String description, Consumer<String> target) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder(name)
                .order(order)
                .description(description)
                .paramLabel(label)
                .required(true)
                .type(String.class)
                .converters(new ResolvedValueConverter())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        target.accept((String) value);
                        return value;
                    }
                })
                .build());
    }

    private void handleForceStartParam(CommandLine.Model.CommandSpec spec, int order, StartTaskCommandExecutor executor) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder("--force-start")
                .order(order)
                .description("Operator override for manual re-runs: run a version that is not APPROVED in Registry, and replace an open "
                        + "activity that cannot accept the task. It closes the activity of a run that may still be alive: do not set it in pipeline files by default.")
                .arity("0")
                .type(boolean.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setForceStart(Boolean.TRUE.equals(value));
                        return value;
                    }
                })
                .build());
    }

    private void handleDurationParam(CommandLine.Model.CommandSpec spec, int order, String name, String description,
                                     Duration minimum, Consumer<Duration> target) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder(name)
                .order(order)
                .description(description)
                .paramLabel("DURATION")
                .required(false)
                .type(Duration.class)
                .converters(new DurationConverter(minimum))
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        target.accept((Duration) value);
                        return value;
                    }
                })
                .build());
    }

    @Override
    public String getParentCommandName() {
        return DEVOPS_COMMAND;
    }

    @Override
    public String getCommandName() {
        return DEVOPS_START_TASK_COMMAND;
    }

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}
