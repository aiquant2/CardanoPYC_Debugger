package com.haskell.ghcid;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class TriggerDebugAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Messages.showInfoMessage("Triggering Debug Configuration", "Info");

        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings selectedConfig = runManager.getSelectedConfiguration();

        if (selectedConfig != null) {
            ProgramRunnerUtil.executeConfiguration(
                    selectedConfig, DefaultDebugExecutor.getDebugExecutorInstance()
            );
        } else {
            Messages.showErrorDialog("No selected configuration to debug!", "Error");
        }
    }
}
