//package com.pyc.cardanopyc_debugger.diagnostic;
//
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.wm.StatusBar;
//import com.intellij.openapi.wm.StatusBarWidget;
//import com.intellij.openapi.wm.StatusBarWidgetFactory;
//import com.intellij.ui.IconManager;
//import org.jetbrains.annotations.NonNls;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import javax.swing.*;
//
//public class HaskellStatusBarWidget implements StatusBarWidget, StatusBarWidgetFactory {
//    private final JLabel label = new JLabel("Haskell");
//    private StatusBar statusBar;
//
//
//
//    public static HaskellStatusBarWidget getInstance(Project project) {
//        return project.getService(HaskellStatusBarWidget.class);
//    }
//
//    public void setRunning() {
//        update("Haskell (running)", "/icons/sync.svg");
//    }
//
//    public void setSuccess() {
//        update("Haskell (success)", "/icons/check.svg");
//    }
//
//    public void setError() {
//        update("Haskell (error)", "/icons/error.svg");
//    }
//
//    public void setStopped() {
//        update("Haskell (stopped)", null);
//    }
//
//    private void update(String text, @Nullable String iconPath) {
//        label.setText(text);
//        if (iconPath != null) {
//            label.setIcon(IconManager.getInstance().getIcon(iconPath, HaskellStatusBarWidget.class));
//        } else {
//            label.setIcon(null);
//        }
//        if (statusBar != null) {
//            statusBar.updateWidget(ID());
//        }
//    }
//
//    // StatusBarWidget implementation
//    @Override
//    public @NonNls @NotNull String ID() {
//        return "HaskellStatusWidget";
//    }
//
//    @Override
//    public @Nullable WidgetPresentation getPresentation() {
//        return null;
//    }
//
//
//    public @NotNull JComponent getComponent() {
//        return label;
//    }
//
//    @Override
//    public void install(@NotNull StatusBar statusBar) {
//        this.statusBar = statusBar;
//        statusBar.addWidget(this, "after Position");
//    }
//
//    @Override
//    public void dispose() {
//        if (statusBar != null) {
//            statusBar.removeWidget(ID());
//        }
//    }
//
//    // StatusBarWidgetFactory implementation
//    @Override
//    public @NotNull String getId() {
//        return ID();
//    }
//
//    @Override
//    public @NotNull String getDisplayName() {
//        return "Haskell Status";
//    }
//
//    @Override
//    public boolean isAvailable(@NotNull Project project) {
//        return true;
//    }
//
//    @Override
//    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
//        return new HaskellStatusBarWidget();
//    }
//
//    @Override
//    public void disposeWidget(@NotNull StatusBarWidget widget) {
//        widget.dispose();
//    }
//
//    @Override
//    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
//        return true;
//    }
//}
package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HaskellStatusBarWidget implements StatusBarWidget {
    private final JLabel label = new JLabel("Haskell");
    private StatusBar statusBar;

    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
        statusBar.addWidget(this, "after Position");
    }

    public void setRunning() {
        update("Haskell (running)", "/icons/sync.svg");
    }

    public void setSuccess() {
        update("Haskell (success)", "/icons/check.svg");
    }

    public void setError() {
        update("Haskell (error)", "/icons/error.svg");
    }

    public void setStopped() {
        update("Haskell (stopped)", null);
    }

    private void update(String text, @Nullable String iconPath) {
        label.setText(text);
        if (iconPath != null) {
            label.setIcon(IconManager.getInstance().getIcon(iconPath, HaskellStatusBarWidget.class));
        } else {
            label.setIcon(null);
        }
        if (statusBar != null) {
            statusBar.updateWidget(ID());
        }
    }

    @Override
    public @NonNls @NotNull String ID() {
        return "HaskellStatusWidget";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return null;
    }


    public @NotNull JComponent getComponent() {
        return label;
    }

    @Override
    public void dispose() {
        if (statusBar != null) {
            statusBar.removeWidget(ID());
        }
    }
}
