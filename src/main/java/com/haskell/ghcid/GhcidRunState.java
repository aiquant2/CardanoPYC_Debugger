//package com.haskell.ghcid;
//
//
//import com.intellij.execution.ExecutionException;
//import com.intellij.execution.Executor;
//import com.intellij.execution.configurations.RunProfileState;
//
//import com.intellij.execution.impl.ConsoleViewImpl;
//import com.intellij.execution.process.ProcessHandler;
//import com.intellij.execution.runners.ExecutionEnvironment;
//import com.intellij.execution.runners.ProgramRunner;
//import com.intellij.execution.ui.ConsoleViewContentType;
//import com.intellij.execution.ui.ConsoleView;
//import com.intellij.execution.DefaultExecutionResult;
//import com.intellij.execution.ExecutionResult;
//import com.intellij.openapi.project.Project;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.*;
//
//public class GhcidRunState implements RunProfileState {
//
//    private final Project project;
//    private final ExecutionEnvironment environment;
//
//    public GhcidRunState(Project project, ExecutionEnvironment environment) {
//        this.project = project;
//        this.environment = environment;
//        System.out.println("ghcid run state");
//    }
//
//    @Override
//    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
//        ConsoleView consoleView = new ConsoleViewImpl(project, true);
//        ProcessBuilder pb = new ProcessBuilder("ghcid", "--command=cabal repl");
//
//        try {
//            Process process = pb.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            new Thread(() -> {
//                String line;
//                try {
//                    while ((line = reader.readLine()) != null) {
//                        consoleView.print(line + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
//                    }
//                } catch (IOException e) {
//                    consoleView.print("ERROR: " + e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
//                }
//            }).start();
//            System.out.println("ghcid run state 2");
//
//            @NotNull ProcessHandler processHandler = null;
//            return new DefaultExecutionResult(consoleView, processHandler);
//
//        } catch (IOException e) {
//            throw new ExecutionException("Failed to start ghcid", e);
//        }
//    }
//}





// new

package com.haskell.ghcid;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;

public class GhcidRunState extends CommandLineState {
    private final Project project;

    public GhcidRunState(Project project, ExecutionEnvironment environment) {
        super(environment);
        this.project = project;
    }

    @Override
    protected OSProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine("ghcid", "--command=cabal repl");
        commandLine.setWorkDirectory(project.getBasePath());
        return new OSProcessHandler(commandLine);
    }
}
