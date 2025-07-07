package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileOpenListener implements BulkFileListener {
    private final Project project;
    private final GhcidProcessHandler ghcidProcess;

    public FileOpenListener(Project project, GhcidProcessHandler ghcidProcess) {
        this.project = project;
        this.ghcidProcess = ghcidProcess;
        project.getMessageBus().connect().subscribe(
                VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (file != null && "hs".equalsIgnoreCase(file.getExtension())) {
                ghcidProcess.startGhcid();
                break;
            }
        }
    }
}

