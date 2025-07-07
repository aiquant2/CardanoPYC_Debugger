package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

public class GhcidProcessHandler extends ProcessHandler {
    private final Project project;
    private Process process;
    private final StringBuilder outputBuffer = new StringBuilder();
    private long lastStartTime = 0;

    public GhcidProcessHandler(Project project) {
        this.project = project;
    }

    public void startGhcid() {
        long now = System.currentTimeMillis();
        if (now - lastStartTime < 1000) return; // debounce: 1s cooldown
        lastStartTime = now;

        stopGhcid();

        String projectPath = project.getBasePath();
        if (projectPath == null) {
            NotificationHelper.showError(project, "No project directory found");
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("ghcid", "--command", "cabal repl");
            pb.directory(Paths.get(projectPath).toFile());
            process = pb.start();

            addProcessListener(new ProcessListener() {
                @Override
                public void startNotified(@NotNull ProcessEvent event) {
                    HaskellStatusBarWidget widget = project.getService(HaskellStatusBarService.class).getWidget();
                    if (widget != null) {
                        widget.setRunning();
                    }                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    HaskellStatusBarWidget widget = project.getService(HaskellStatusBarService.class).getWidget();
                    if (widget != null) {
                        widget.setRunning();
                    }                }

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {}
            });

            // Read ghcid output asynchronously
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuffer.append(line).append("\n");
                        processGhcidOutput(line);
                    }
                } catch (IOException e) {
                    NotificationHelper.showError(project, "Error reading ghcid output: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            NotificationHelper.showError(project, "Failed to start ghcid: " + e.getMessage());
            HaskellStatusBarWidget widget = project.getService(HaskellStatusBarService.class).getWidget();
            if (widget != null) {
                widget.setRunning();
            }        }
    }

    public void stopGhcid() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    private void processGhcidOutput(String output) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (output.contains("All good")) {
                HaskellStatusBarWidget widget = project.getService(HaskellStatusBarService.class).getWidget();
                if (widget != null) {
                    widget.setRunning();
                }                ProblemManager.getInstance(project).clearProblems();
            } else {
                List<HaskellError> errors = ErrorParser.parseGhcidOutput(output);
                if (!errors.isEmpty()) {
                    HaskellStatusBarWidget widget = project.getService(HaskellStatusBarService.class).getWidget();
                    if (widget != null) {
                        widget.setRunning();
                    }                    ProblemManager.getInstance(project).showProblems(errors);
                }
            }
        });
    }

    @Override
    protected void destroyProcessImpl() {
        stopGhcid();
    }

    @Override
    protected void detachProcessImpl() {
        stopGhcid();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }
    @Override
    public OutputStream getProcessInput() {
        return process != null ? process.getOutputStream() : null;
    }

}
