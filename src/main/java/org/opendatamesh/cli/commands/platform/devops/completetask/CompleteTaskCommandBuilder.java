package org.opendatamesh.cli.commands.platform.devops.completetask;

import com.google.common.collect.Lists;
import org.opendatamesh.cli.clients.platform.devops.ExecutionStatus;
import org.opendatamesh.cli.commands.PicoCliCommandBuilder;
import org.opendatamesh.cli.commands.platform.devops.ResolvedValueConverter;
import org.opendatamesh.cli.commands.platform.devops.TaskStatusConverter;
import org.opendatamesh.cli.configs.OdmCliConfiguration;
import org.opendatamesh.cli.usecases.devops.completetask.CompleteTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.function.IntConsumer;

import static org.opendatamesh.cli.commands.platform.devops.DevOpsCommandBuilder.DEVOPS_COMMAND;

@Component
public class CompleteTaskCommandBuilder implements PicoCliCommandBuilder {
    public static final String DEVOPS_COMPLETE_TASK_COMMAND = "complete-task";

    @Autowired
    @Lazy
    private List<PicoCliCommandBuilder> commands;

    @Autowired
    private OdmCliConfiguration odmCliConfiguration;

    @Autowired
    private CompleteTaskFactory completeTaskFactory;

    @Override
    public CommandLine buildCommand(String... args) {
        CompleteTaskCommandExecutor executor = new CompleteTaskCommandExecutor(odmCliConfiguration, completeTaskFactory);
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.wrapWithoutInspection(executor);
        spec.name(DEVOPS_COMPLETE_TASK_COMMAND);
        spec.usageMessage().description("Command to complete a running task of a DevOps activity, addressed by the pipeline's run id: "
                + "with --status it uploads logs and results and records the final status of an instrumented task; without --status it uploads "
                + "only the results of a full-control task.");
        spec.mixinStandardHelpOptions(true);

        handleWithOrder(Lists.newArrayList(
                order -> handleProviderRunIdParam(spec, order, executor),
                order -> handleStatusParam(spec, order, executor),
                order -> handleLogsFileParam(spec, order, executor),
                order -> handleResultsFileParam(spec, order, executor)
        ));

        commands.stream().filter(command -> DEVOPS_COMPLETE_TASK_COMMAND.equals(command.getParentCommandName()))
                .forEach(command -> spec.addSubcommand(command.getCommandName(), command.buildCommand(args)));
        return new CommandLine(spec);
    }

    private void handleProviderRunIdParam(CommandLine.Model.CommandSpec spec, int order, CompleteTaskCommandExecutor executor) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder("-r", "--provider-run-id")
                .order(order)
                .description("Run id of the task to complete. For an instrumented task, the value given to start-task; "
                        + "for a full-control task, the pipeline id. In a child pipeline, pass the parent's id.")
                .paramLabel("RUN_ID")
                .required(true)
                .type(String.class)
                .converters(new ResolvedValueConverter())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setProviderRunId((String) value);
                        return value;
                    }
                })
                .build());
    }

    private void handleStatusParam(CommandLine.Model.CommandSpec spec, int order, CompleteTaskCommandExecutor executor) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder("-s", "--status")
                .order(order)
                .description("Final status of an instrumented task: SUCCEEDED, FAILED or CANCELED. Without it, the command uploads "
                        + "only the results of a full-control task, and --results-file is required.")
                .paramLabel("STATUS")
                .type(ExecutionStatus.class)
                .converters(new TaskStatusConverter())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setStatus((ExecutionStatus) value);
                        return value;
                    }
                })
                .build());
    }

    private void handleLogsFileParam(CommandLine.Model.CommandSpec spec, int order, CompleteTaskCommandExecutor executor) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder("-l", "--logs-file")
                .order(order)
                .description("File whose content is uploaded as one log row, for an instrumented task. Repeatable. Needs --status.")
                .paramLabel("FILE")
                .arity("1")
                .type(List.class)
                .auxiliaryTypes(String.class)
                .getter(new CommandLine.Model.IGetter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T get() {
                        return (T) executor.getLogFiles();
                    }
                })
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T set(T value) {
                        executor.setLogFiles((List<String>) value);
                        return value;
                    }
                })
                .build());
    }

    private void handleResultsFileParam(CommandLine.Model.CommandSpec spec, int order, CompleteTaskCommandExecutor executor) {
        spec.addOption(CommandLine.Model.OptionSpec
                .builder("-o", "--results-file")
                .order(order)
                .description("File with the results, a single JSON object. Required without --status.")
                .paramLabel("FILE")
                .type(String.class)
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        executor.setResultsFile((String) value);
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
        return DEVOPS_COMPLETE_TASK_COMMAND;
    }

    private void handleWithOrder(List<IntConsumer> handlers) {
        int order = 0;
        for (IntConsumer handler : handlers) {
            handler.accept(order);
            order++;
        }
    }
}
