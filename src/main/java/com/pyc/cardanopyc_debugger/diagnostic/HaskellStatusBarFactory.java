package com.pyc.cardanopyc_debugger.diagnostic;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

public class HaskellStatusBarFactory implements StatusBarWidgetFactory {
    @Override
    public @NotNull String getId() {
        return "HaskellStatusWidget";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Haskell Status";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        HaskellStatusBarWidget widget = new HaskellStatusBarWidget();
        project.getService(HaskellStatusBarService.class).setWidget(widget); // <--- Important!
        return widget;
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
