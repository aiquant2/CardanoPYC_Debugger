package com.haskell.ghcid;

import com.haskell.ghcid.GhcidRunner;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class StopGhcidAction extends AnAction {
    @Override
    public void actionPerformed(@org.jetbrains.annotations.NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            GhcidRunner.getInstance(project).stop();
        }
    }

//    @Override
//    public void update(@org.jetbrains.annotations.NotNull AnActionEvent e) {
//        Project project = e.getProject();
//        e.getPresentation().setEnabledAndVisible(project != null);
//    }
@Override
public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    boolean enabled = project != null && GhcidRunner.getInstance(project).isRunning();
    e.getPresentation().setEnabledAndVisible(enabled);
}

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }}