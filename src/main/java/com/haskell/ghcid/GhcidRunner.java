

package com.haskell.ghcid;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GhcidRunner implements Disposable {
    private static final Logger LOG = Logger.getInstance(GhcidRunner.class);

    private final Project project;
    private OSProcessHandler processHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<VirtualFile, RangeHighlighter> highlighters = new HashMap<>();
    private StatusBarWidget statusWidget;

    private static final TextAttributes ERROR_ATTRIBUTES = new TextAttributes(
            null, JBColor.RED, null, EffectType.ROUNDED_BOX, Font.PLAIN);
    private static final TextAttributes WARNING_ATTRIBUTES = new TextAttributes(
            null, JBColor.ORANGE, null, EffectType.ROUNDED_BOX, Font.PLAIN);

    public GhcidRunner(Project project) {
        this.project = project;
    }

    public void start() {
        if (isRunning.getAndSet(true)) return;

        updateStatus("Haskell $(sync~spin)", "Checking for errors...");

        try {
            GeneralCommandLine commandLine = new GeneralCommandLine()
                    .withExePath("ghcid")
                    .withParameters("--command", "cabal repl")
                    .withWorkDirectory(project.getBasePath());

            processHandler = new OSProcessHandler(commandLine);
            Disposer.register(this, () -> {
                if (!processHandler.isProcessTerminated()) {
                    processHandler.destroyProcess();
                }
            });

            processHandler.addProcessListener(new ProcessAdapter() {
                private final List<String> block = new ArrayList<>();

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String line = event.getText().trim();
                    if (line.matches(".*\\.hs:\\d+:\\d+: error:.*")) {
                        if (!block.isEmpty()) {
                            processGhcidOutputBlock(block.toArray(new String[0]));
                            block.clear();
                        }
                    }
                    if (!line.isEmpty()) {
                        block.add(line);
                    }
                    if (line.isEmpty() && !block.isEmpty()) {
                        processGhcidOutputBlock(block.toArray(new String[0]));
                        block.clear();
                    }
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    isRunning.set(false);
                    if (!block.isEmpty()) {
                        processGhcidOutputBlock(block.toArray(new String[0]));
                        block.clear();
                    }
                    if (event.getExitCode() == 0) {
                        updateStatus("Haskell $(check)", "No errors found");
                        clearAllHighlights();
                    } else {
                        updateStatus("Haskell $(error)", "Errors detected");
                    }
                }
            });

            processHandler.startNotify();
        } catch (ExecutionException e) {
            LOG.error("Failed to start ghcid", e);
            updateStatus("Haskell $(error)", "Failed to start");
            isRunning.set(false);
        }
    }

    private void processGhcidOutputBlock(String[] lines) {
        if (lines.length == 0) return;
        Pattern pattern = Pattern.compile(
                "^(?<file>.+?):(?<line>\\d+):(?<column>\\d+):\\s*(?<type>error|warning|\\[error\\]|\\[warning\\]):?");
        Matcher matcher = pattern.matcher(lines[0]);
        if (!matcher.find()) return;

        String filePath = matcher.group("file").trim();
        int lineNumber = Integer.parseInt(matcher.group("line")) - 1;
        int columnNumber = Integer.parseInt(matcher.group("column")) - 1;
        String errorType = matcher.group("type");

        StringBuilder messageBuilder = new StringBuilder();
        for (String line : lines) {
            messageBuilder.append(line).append("\n");
        }

        String fullMessage = messageBuilder.toString().trim();
        VirtualFile file = findVirtualFile(filePath);
        if (file != null) {
            highlightError(file, lineNumber, columnNumber, errorType, fullMessage);
        }
    }

//    private void highlightError(VirtualFile file, int lineNumber, int columnNumber, String errorType, String message) {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            Document document = FileDocumentManager.getInstance().getDocument(file);
//            if (document == null) return;
//
//            Editor editor = FileEditorManager.getInstance(project).openTextEditor(
//                    new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, lineNumber, columnNumber), false);
//            if (editor == null) return;
//
//            RangeHighlighter oldHighlighter = highlighters.get(file);
//            if (oldHighlighter != null) {
//                editor.getMarkupModel().removeHighlighter(oldHighlighter);
//            }
//
//            int lineStart = document.getLineStartOffset(lineNumber);
//            int lineEnd = document.getLineEndOffset(lineNumber);
//            int startOffset = lineStart + columnNumber;
//            int endOffset = startOffset + 1;
//
//            String lineText = document.getText().substring(lineStart, lineEnd);
//            if (columnNumber < lineText.length()) {
//                int wordStart = columnNumber;
//                while (wordStart > 0 && !Character.isWhitespace(lineText.charAt(wordStart - 1))) wordStart--;
//                int wordEnd = columnNumber;
//                while (wordEnd < lineText.length() && !Character.isWhitespace(lineText.charAt(wordEnd))) wordEnd++;
//
//                startOffset = lineStart + wordStart;
//                endOffset = lineStart + wordEnd;
//            }
//
//            TextAttributes attributes = errorType.toLowerCase().contains("error") ? ERROR_ATTRIBUTES : WARNING_ATTRIBUTES;
//
//            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
//                    startOffset, endOffset,
//                    HighlighterLayer.ERROR,
//                    attributes,
//                    HighlighterTargetArea.EXACT_RANGE);
//
//            highlighter.setErrorStripeTooltip(message);
//
//            highlighters.put(file, highlighter);
//            updateStatus("Haskell $(error)", "Errors detected");
//        });
//    }
private void highlightError(VirtualFile file, int lineNumber, int columnNumber, String errorType, String message) {
    ApplicationManager.getApplication().invokeLater(() -> {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return;

        Editor editor = FileEditorManager.getInstance(project).openTextEditor(
                new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, lineNumber, columnNumber), false);
        if (editor == null) return;

        RangeHighlighter oldHighlighter = highlighters.get(file);
        if (oldHighlighter != null) {
            editor.getMarkupModel().removeHighlighter(oldHighlighter);
        }

        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        int startOffset = lineStart + columnNumber;
        int endOffset = startOffset + 1;

        String lineText = document.getText().substring(lineStart, lineEnd);
        if (columnNumber < lineText.length()) {
            int wordStart = columnNumber;
            while (wordStart > 0 && !Character.isWhitespace(lineText.charAt(wordStart - 1))) wordStart--;
            int wordEnd = columnNumber;
            while (wordEnd < lineText.length() && !Character.isWhitespace(lineText.charAt(wordEnd))) wordEnd++;

            startOffset = lineStart + wordStart;
            endOffset = lineStart + wordEnd;
        }

        TextAttributes attributes = errorType.toLowerCase().contains("error") ? ERROR_ATTRIBUTES : WARNING_ATTRIBUTES;

        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                startOffset, endOffset,
                HighlighterLayer.ERROR,
                attributes,
                HighlighterTargetArea.EXACT_RANGE);

        highlighter.setErrorStripeTooltip(message);
        highlighter.setThinErrorStripeMark(true);
        highlighter.getErrorStripeTooltip();
        highlighter.setErrorStripeMarkColor(Color.yellow);
        highlighter.setGutterIconRenderer(new GutterIconRenderer() {
            @Override
            public @NotNull Icon getIcon() {
                return errorType.toLowerCase().contains("error")
                        ? com.intellij.icons.AllIcons.General.Error
                        : com.intellij.icons.AllIcons.General.Warning;
            }

            @Override
            public String getTooltipText() {
                return message;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });

        highlighters.put(file, highlighter);
        updateStatus("Haskell $(error)", "Errors detected");
    });
}

    private VirtualFile findVirtualFile(String filePath) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return null;
        java.nio.file.Path resolvedPath = Paths.get(project.getBasePath()).resolve(filePath).normalize();
        return baseDir.getFileSystem().findFileByPath(resolvedPath.toString());
    }

    private void clearAllHighlights() {
        ApplicationManager.getApplication().invokeLater(() -> {
            for (Map.Entry<VirtualFile, RangeHighlighter> entry : highlighters.entrySet()) {
                VirtualFile file = entry.getKey();
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (editor != null) {
                    editor.getMarkupModel().removeHighlighter(entry.getValue());
                }
            }
            highlighters.clear();
        });
    }

    private void updateStatus(String text, String tooltip) {
        ApplicationManager.getApplication().invokeLater(() -> {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                if (statusWidget != null) {
                    statusBar.removeWidget(statusWidget.ID());
                }

                statusWidget = new StatusBarWidget() {
                    @Override
                    public @NotNull String ID() {
                        return "HaskellGhcidStatus";
                    }

                    @Override
                    public @Nullable WidgetPresentation getPresentation() {
                        return new WidgetPresentation() {
                            public @Nullable String getText() {
                                return text;
                            }

                            @Override
                            public @Nullable String getTooltipText() {
                                return tooltip;
                            }

                            @Override
                            public @Nullable Consumer<MouseEvent> getClickConsumer() {
                                return null;
                            }
                        };
                    }

                    @Override
                    public void install(@NotNull StatusBar statusBar) {}

                    @Override
                    public void dispose() {}
                };

                statusBar.addWidget(statusWidget, "before Position");
            }
        });
    }

    @Override
    public void dispose() {
        stop();
        if (statusWidget != null) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                statusBar.removeWidget(statusWidget.ID());
            }
        }
    }

    public void stop() {
        if (processHandler != null) {
            processHandler.destroyProcess();
        }
        isRunning.set(false);
        clearAllHighlights();
        updateStatus("Haskell", "Idle");
    }

    public void startIfNeeded() {
        if (!isRunning.get() && isCabalProject()) {
            start();
        }
    }

    public boolean isCabalProject() {
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) return false;

        return projectDir.findChild("cabal.project") != null ||
                projectDir.findChild("cabal.project.freeze") != null ||
                hasCabalFiles(projectDir);
    }

    private boolean hasCabalFiles(VirtualFile dir) {
        VirtualFile[] children = dir.getChildren();
        for (VirtualFile file : children) {
            if (file.isDirectory()) {
                if (hasCabalFiles(file)) return true;
            } else if (file.getName().endsWith(".cabal")) {
                return true;
            }
        }
        return false;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public static GhcidRunner getInstance(Project project) {
        return project.getService(GhcidRunner.class);
    }

    public Project getProject() {
        return this.project;
    }
}
