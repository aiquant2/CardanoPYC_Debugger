package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class FileChangeListener implements FileDocumentManagerListener {
    private final Project project;
    private final GhcidProcessHandler ghcidProcess;

    public FileChangeListener(Project project, GhcidProcessHandler ghcidProcess) {
        this.project = project;
        this.ghcidProcess = ghcidProcess;
        project.getMessageBus().connect().subscribe(
                FileDocumentManagerListener.TOPIC, this);
    }

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file != null && "hs".equalsIgnoreCase(file.getExtension())) {
            ghcidProcess.startGhcid();
        }
    }
}
