package com.debug_tools;

import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CabalDebugSettingsEditor extends SettingsEditor<CabalDebugConfiguration> {
    private final JPanel panel = new JPanel();

    @Override
    protected void resetEditorFrom(@NotNull CabalDebugConfiguration configuration) {}

    @Override
    protected void applyEditorTo(@NotNull CabalDebugConfiguration configuration) {}

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }
}