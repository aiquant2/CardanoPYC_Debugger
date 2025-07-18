package com.haskell.ghcid;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;

public class HaskellFileListener implements FileEditorManagerListener {

    public static void main(String[] args) {

    }
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

//    private void runGhcidIfHaskell(VirtualFile file, Project project) {
//        if (file.getName().endsWith(".hs")) {
//            GhcidRunner runner = new GhcidRunner(project);
//            if (runner.isCabalProject() && !runner.isRunning()) {
//                runner.start();
//            }
//        }
//    }

    // new

    private void runGhcidIfHaskell(VirtualFile file, Project project) {
        if (file.getName().endsWith(".hs")) {
            // Optionally start your runner
            GhcidRunner runner = new GhcidRunner(project);
            if (runner.isCabalProject() && !runner.isRunning()) {
                runner.start();
            }

            // Set up the Ghcid debug configuration dynamically
            RunManager runManager = RunManager.getInstance(project);

            // Check if configuration already exists
            boolean alreadyExists = runManager.getAllSettings().stream()
                    .anyMatch(config -> config.getName().equals("AutoDebugGhcid"));

            if (!alreadyExists) {
                GhcidRunConfigurationType configType = new GhcidRunConfigurationType();

                RunnerAndConfigurationSettings settings =
                        runManager.createConfiguration("AutoDebugGhcid", configType.getConfigurationFactories()[0]);

                runManager.addConfiguration(settings);
                runManager.setSelectedConfiguration(settings);

                System.out.println("[Ghcid Debug] AutoDebugGhcid configuration created and selected.");
            } else {
                // Just select it if already exists
                RunnerAndConfigurationSettings existingConfig = runManager.getAllSettings().stream()
                        .filter(config -> config.getName().equals("AutoDebugGhcid"))
                        .findFirst()
                        .orElse(null);

                if (existingConfig != null) {
                    runManager.setSelectedConfiguration(existingConfig);
                    System.out.println("[Ghcid Debug] AutoDebugGhcid configuration already exists. Selected.");
                }
            }
        }
    }

}
