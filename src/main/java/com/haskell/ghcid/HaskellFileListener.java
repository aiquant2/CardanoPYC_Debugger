package com.haskell.ghcid;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HaskellFileListener implements Disposable {
    private final Project project;
    private final GhcidRunner ghcidRunner;
    private MessageBusConnection connection;

    public HaskellFileListener(Project project, GhcidRunner ghcidRunner) {
        this.project = project;
        this.ghcidRunner = ghcidRunner;
    }

    public void setupListeners() {
        // Listen for file opening
        connection = project.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                if (isHaskellFile(file)) {
                    ghcidRunner.startIfNeeded();
                }
            }
        });

        // Listen for file changes
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    VirtualFile file = event.getFile();
                    if (file != null && isHaskellFile(file)) {
                        ghcidRunner.startIfNeeded();
                        break;
                    }
                }
            }
        });

        // Check current file on startup
        VirtualFile[] openFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        if (openFiles.length > 0 && isHaskellFile(openFiles[0])) {
            ghcidRunner.startIfNeeded();
        }
    }

    private boolean isHaskellFile(VirtualFile file) {
        return file != null && "hs".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public void dispose() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}