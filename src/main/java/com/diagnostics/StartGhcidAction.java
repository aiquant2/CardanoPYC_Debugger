package com.diagnostics;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StartGhcidAction extends AnAction {
    @Override
        public void actionPerformed(@NotNull AnActionEvent e){

        System.out.println("call");

        Project project = e.getProject();

        if (project != null) {
            GhcidRunner.getInstance(project);
            GhcidRunner.getInstance(project).start();

        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }


    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}


