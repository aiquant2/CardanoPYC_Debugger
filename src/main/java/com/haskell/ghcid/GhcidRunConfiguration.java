//package com.haskell.ghcid;
//
//
//import com.intellij.execution.configurations.RunConfiguration;
//import com.intellij.execution.configurations.RunConfigurationBase;
//import com.intellij.execution.configurations.ConfigurationFactory;
//import com.intellij.execution.runners.ExecutionEnvironment;
//import com.intellij.execution.Executor;
//import com.intellij.execution.ExecutionException;
//import com.intellij.execution.runners.ProgramRunner;
//import com.intellij.execution.configurations.RunProfileState;
//import com.intellij.openapi.options.SettingsEditor;
//import com.intellij.openapi.project.Project;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import javax.naming.ConfigurationException;
//import javax.swing.*;
//
//public  class GhcidRunConfiguration extends RunConfigurationBase<Object> {
//    public GhcidRunConfiguration(Project project, ConfigurationFactory factory, String name) {
//        super(project, factory, name);
//    }
//
//    @Override
//    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
//        return new GhcidRunState(getProject(), environment);
//    }
//
//    @Override
//    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
//        // You can return an empty editor if you don't need any config UI
//        return new SettingsEditor<RunConfiguration>() {
//            @Override
//            protected void resetEditorFrom(@NotNull RunConfiguration s) {
//                // no-op
//            }
//
//            @Override
//            protected void applyEditorTo(@NotNull RunConfiguration s) {
//                // no-op
//            }
//
//            @NotNull
//            @Override
//            public JComponent createEditor() {
//                return new JPanel(); // empty panel, as placeholder
//            }
//        };
//    }
//
//}




// new


package com.haskell.ghcid;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GhcidRunConfiguration extends RunConfigurationBase<Object> {
    public GhcidRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
        System.out.println("ghcid run configuration 1");
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
            throws ExecutionException {
        System.out.println("ghcid run configuration 2");
        return new GhcidRunState(getProject(), environment);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new SettingsEditor<RunConfiguration>() {
            @Override
            protected void resetEditorFrom(@NotNull RunConfiguration s) {}

            @Override
            protected void applyEditorTo(@NotNull RunConfiguration s) {}

            @NotNull
            @Override
            public JComponent createEditor() {
                System.out.println("ghcid run configuration 3");
                return new JPanel(); // Empty UI panel
            }
        };
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        String basePath = getProject().getBasePath();
        if (basePath == null || basePath.isEmpty()) {
            throw new RuntimeConfigurationError("Project base path is not set.");
        }
    }
}
