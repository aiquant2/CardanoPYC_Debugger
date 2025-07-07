
package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class NotificationHelper {
    private static final String GROUP_ID = "CardanoPyC Notifications";

    public static void showError(Project project, String message) {
        Notifications.Bus.notify(new Notification(GROUP_ID, "Error", message, NotificationType.ERROR), project);
    }

    public static void showInfo(Project project, String message) {
        Notifications.Bus.notify(new Notification(GROUP_ID, "Info", message, NotificationType.INFORMATION), project);
    }

    public static void showWarning(Project project, String message) {
        Notifications.Bus.notify(new Notification(GROUP_ID, "Warning", message, NotificationType.WARNING), project);
    }
}
