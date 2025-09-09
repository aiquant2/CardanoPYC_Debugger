
package com.debug_tools;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

public class CabalDebugRunner extends AsyncProgramRunner<RunnerSettings> {

    @NotNull
    @Override
    public String getRunnerId() {
        return "CabalDebugRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID)
                && profile instanceof CabalDebugConfiguration;
    }

    @NotNull
    @Override
    protected Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment,
                                                    @NotNull RunProfileState state) {
        try {
            ExecutionResult executionResult = state.execute(environment.getExecutor(), this);

            if (executionResult == null) {
                return Promises.rejectedPromise(new ExecutionException("ExecutionResult is null"));
            }

            // Ensure console exists
            if (executionResult.getExecutionConsole() == null) {
                return Promises.rejectedPromise(new ExecutionException("ExecutionConsole is null"));
            }

            RunContentDescriptor descriptor = new RunContentDescriptor(
                    executionResult.getExecutionConsole(),
                    executionResult.getProcessHandler(),
                    executionResult.getExecutionConsole().getComponent(), // ✅ not null now
                    environment.getRunProfile().getName()
            );

            return Promises.resolvedPromise(descriptor);
        } catch (ExecutionException e) {
            return Promises.rejectedPromise(e);
        }
    }
}
 