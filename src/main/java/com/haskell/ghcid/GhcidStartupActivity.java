package com.haskell.ghcid;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class GhcidStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        // Check if this is a Cabal project
        if (isCabalProject(project)) {
            GhcidRunner.getInstance(project).startIfNeeded();
        }
    }

    private boolean isCabalProject(Project project) {
        // Check for cabal.project or cabal.project.freeze file
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) return false;

        VirtualFile cabalProject = projectDir.findChild("cabal.project");
        VirtualFile cabalProjectFreeze = projectDir.findChild("cabal.project.freeze");

        // Alternatively, check for .cabal files in the project
        return cabalProject != null || cabalProjectFreeze != null ||
                projectDir.findChild("*.cabal") != null;
    }
}