package com.haskell.ghcid;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class TriggerGhcidDebugAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        RunManager runManager = RunManager.getInstance(project);

        GhcidRunConfigurationType configType = new GhcidRunConfigurationType();
        RunnerAndConfigurationSettings settings =
                runManager.createConfiguration("AutoDebugGhcid", configType.getConfigurationFactories()[0]);

        runManager.addConfiguration(settings);
        runManager.setSelectedConfiguration(settings);

        Messages.showInfoMessage(project, "Starting automatic Ghcid debug config...", "Ghcid Debug");

        ProgramRunnerUtil.executeConfiguration(settings, DefaultDebugExecutor.getDebugExecutorInstance());
    }
}
