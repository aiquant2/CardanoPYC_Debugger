package com.diagnostics;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
//
//public class HaskellFileListener implements FileEditorManagerListener {
//
//    @Override
//    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//        runGhcidIfHaskell(file, source.getProject());
//    }
//
//    @Override
//    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
//        VirtualFile file = event.getNewFile();
//        if (file != null) {
//            runGhcidIfHaskell(file, event.getManager().getProject());
//        }
//    }
//
//    private void runGhcidIfHaskell(VirtualFile file, Project project) {
//        if (file.getName().endsWith(".hs")) {
//            GhcidRunner runner = new GhcidRunner(project);
//            if (runner.isCabalProject() && !runner.isRunning()) {
//                runner.start();
//            }
//        }
//    }
//}

public class HaskellFileListener implements FileEditorManagerListener {
    private final java.util.Map<Project, com.intellij.util.Alarm> alarms = new java.util.HashMap<>();

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runGhcidIfHaskell(file, source.getProject());
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile file = event.getNewFile();
        if (file != null) {
            Project project = event.getManager().getProject();
            // Debounce to prevent rapid restarts
            alarms.computeIfAbsent(project, p -> new com.intellij.util.Alarm())
                    .cancelAllRequests();
            alarms.get(project).addRequest(() -> {
                runGhcidIfHaskell(file, project);
            }, 300); // 300ms debounce
        }
    }

    private void runGhcidIfHaskell(VirtualFile file, Project project) {
        if (file.getName().endsWith(".hs")) {
            GhcidRunner runner = GhcidRunner.getInstance(project);
            if (runner.isCabalProject() && !runner.isRunning()) {
                runner.start();
            }
        }
    }
}