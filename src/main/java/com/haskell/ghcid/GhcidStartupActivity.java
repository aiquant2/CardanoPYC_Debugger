package com.haskell.ghcid;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GhcidStartupActivity implements ProjectActivity {

    @Override
    public @NotNull Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        RunManager runManager = RunManager.getInstance(project);
        ConfigurationFactory factory = GhcidRunConfigurationType.getInstance().getConfigurationFactories()[0];

        // Check if Ghcid config already exists
        List<RunConfiguration> existingConfigs = runManager.getAllConfigurationsList();
        boolean found = existingConfigs.stream()
                .anyMatch(config -> config.getType() instanceof GhcidRunConfigurationType);

        if (!found) {
            RunnerAndConfigurationSettings settings = runManager.createConfiguration("Run with Ghcid", factory);
            runManager.addConfiguration(settings);
            runManager.setSelectedConfiguration(settings);
        }

        // Return Unit.INSTANCE to satisfy Kotlin coroutine interop
        return Unit.INSTANCE;
    }
}
