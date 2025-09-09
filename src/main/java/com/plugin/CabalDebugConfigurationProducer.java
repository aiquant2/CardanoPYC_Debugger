package com.plugin;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CabalDebugConfigurationProducer extends RunConfigurationProducer<CabalDebugConfiguration> {
    protected CabalDebugConfigurationProducer() {
        super(CabalDebugConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(
            @NotNull CabalDebugConfiguration configuration,
            @NotNull ConfigurationContext context,
            @NotNull Ref<PsiElement> sourceElement
    ) {
        if (!CabalProjectDetector.isCabalProject(context.getProject())) {
            return false;
        }
        configuration.setName("Debug Console");
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(
            @NotNull CabalDebugConfiguration configuration,
            @NotNull ConfigurationContext context
    ) {
        return CabalProjectDetector.isCabalProject(context.getProject());
    }
}


