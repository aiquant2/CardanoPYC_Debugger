package com.plugin;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CabalDebugConfigurationType implements ConfigurationType {
    private static final CabalDebugConfigurationType INSTANCE = new CabalDebugConfigurationType();

    public static CabalDebugConfigurationType getInstance() {
        return INSTANCE;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Cabal REPL Debug Configuration";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return "CABAL_REPL_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new CabalDebugConfigurationFactory(this)};
    }

    public static class CabalDebugConfigurationFactory extends ConfigurationFactory {
        protected CabalDebugConfigurationFactory(@NotNull ConfigurationType type) {
            super(type);
        }

        @NotNull
        @Override
        public String getName() {
            return "Cabal REPL";
        }

        @NotNull
        @Override
        public com.intellij.execution.configurations.RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new CabalDebugConfiguration(project, this, "Cabal REPL");
        }
    }
}