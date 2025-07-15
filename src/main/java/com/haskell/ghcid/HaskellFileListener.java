package com.haskell.ghcid;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class HaskellFileListener implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runGhcidIfHaskell(file, source.getProject());
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile file = event.getNewFile();
        if (file != null) {
            runGhcidIfHaskell(file, event.getManager().getProject());
        }
    }

    private void runGhcidIfHaskell(VirtualFile file, Project project) {
        if (file.getName().endsWith(".hs")) {
            GhcidRunner runner = new GhcidRunner(project);
            if (runner.isCabalProject() && !runner.isRunning()) {
                runner.start();
            }
        }
    }
}
