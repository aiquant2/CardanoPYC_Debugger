package com.debug_tools;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import org.jetbrains.annotations.NotNull;
//
//public class CabalDebugRunner extends GenericProgramRunner {
//    @NotNull
//    @Override
//    public String getRunnerId() {
//        return "CabalDebugRunner";
//    }
//
//    @Override
//    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
//        return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) &&
//                profile instanceof CabalDebugConfiguration;
//    }
//}

public class CabalDebugRunner implements ProgramRunner {
    @NotNull
    @Override
    public String getRunnerId() {
        return "CabalDebugRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) &&
                profile instanceof CabalDebugConfiguration;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {

    }
}
