
package com.debug_tools;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;

public class CabalDebugConfiguration extends LocatableConfigurationBase<CommandLineState> {
    protected CabalDebugConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new CabalDebugSettingsEditor();
    }

    public static GeneralCommandLine buildCommandLine(String cabalRoot, boolean isWindows) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setWorkDirectory(cabalRoot);
        commandLine.setCharset(StandardCharsets.UTF_8);

        if (isWindows) {
            commandLine.setExePath("cmd");
            commandLine.addParameters("/c", "echo \u001B[33mStarting GHCi...\u001B[0m && cabal repl");
        } else {
            commandLine.setExePath("/bin/bash");
            String yellow = "\u001B[33m";
            String reset = "\u001B[0m";
            String echoAndRun = "echo -e \"" + yellow + "Starting GHCi..." + reset + "\" && cabal repl";
            commandLine.addParameters("-c", echoAndRun);
        }

        return commandLine;
    }



    @NotNull
    @Override
    public CommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new CommandLineState(environment) {
            @NotNull
            @Override
            protected ProcessHandler startProcess() throws ExecutionException {
                String cabalRoot = CabalProjectDetector.getCabalRoot(getProject());
                if (cabalRoot == null) {
                    throw new ExecutionException("Not a Cabal project");
                }

                GeneralCommandLine commandLine = new GeneralCommandLine();
                commandLine.setWorkDirectory(cabalRoot);
                commandLine.setCharset(StandardCharsets.UTF_8);

                if (SystemInfo.isWindows) {
                    commandLine.setExePath("cmd");
                    // Add ANSI escape for yellow: \u001B[33m
                    commandLine.addParameters("/c", "echo \u001B[33mStarting GHCi...\u001B[0m && cabal repl");
                } else {
                    commandLine.setExePath("/bin/bash");
                    // echo yellow + reset + then run repl
                    String yellow = "\u001B[33m";
                    String reset = "\u001B[0m";
                    String echoAndRun = "echo -e \"" + yellow + "Starting GHCi..." + reset + "\" && cabal repl";
                    commandLine.addParameters("-c", echoAndRun);
                }

                OSProcessHandler processHandler = new OSProcessHandler(commandLine);
                processHandler.setShouldDestroyProcessRecursively(true);

                return processHandler;
            }
        };
    }
}
