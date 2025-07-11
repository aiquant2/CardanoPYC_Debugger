
package com.haskell.ghcid;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
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
    private final Map<VirtualFile, List<RangeHighlighter>> highlighters = new HashMap<>();
    private final Set<VirtualFile> filesWithErrors = new HashSet<>();

    private static final TextAttributes ERROR_ATTRIBUTES = new TextAttributes(
            null, JBColor.RED, null, EffectType.ROUNDED_BOX, Font.PLAIN);
    private static final TextAttributes WARNING_ATTRIBUTES = new TextAttributes(
            null, JBColor.ORANGE, null, EffectType.ROUNDED_BOX, Font.PLAIN);

    public GhcidRunner(Project project) {
        this.project = project;
    }

    public void start() {
        clearAllHighlights();
        clearFixedHighlights();

        try {
            GeneralCommandLine commandLine = new GeneralCommandLine()
                    .withExePath("ghcid")
                    .withParameters("--command", "cabal repl")
                    .withWorkDirectory(project.getBasePath());
            System.out.println("cabal");
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
                    System.out.println(line);
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
                    if (event.getExitCode() == 0) {
                        clearFixedHighlights();
                    }
                }
            });

            processHandler.startNotify();
            attachDocumentListeners();
        } catch (ExecutionException e) {
            LOG.error("Failed to start ghcid", e);
        }
    }

    private void attachDocumentListeners() {
        Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getAllEditors();
        for (Editor editor : editors) {
            Document document = editor.getDocument();
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        FileDocumentManager.getInstance().saveDocument(document);
                        stop();
                        start();
                    });
                }
            }, this);
        }
    }

    private void processGhcidOutputBlock(String[] lines) {
        if (lines.length == 0) return;
        Pattern pattern = Pattern.compile("^(?<file>.+?):(?<line>\\d+):(?<column>\\d+):\\s*(?<type>error|warning|\\[error]|\\[warning]):?");
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
        } else {
            stop();
        }
    }


    private void highlightError(VirtualFile file, int lineNumber, int columnNumber, String errorType, String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) return;

            Editor editor = FileEditorManager.getInstance(project).openTextEditor(
                    new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, lineNumber, columnNumber), false);
            if (editor == null) return;

            int lineStart = document.getLineStartOffset(lineNumber);
            int lineEnd = document.getLineEndOffset(lineNumber);
            String lineText = document.getText().substring(lineStart, lineEnd);

            // Default highlighting range
            int startOffset = lineStart + columnNumber;
            int endOffset = startOffset + 1;

            // Adjust if within line bounds
            if (columnNumber < lineText.length()) {
                String remainingText = lineText.substring(columnNumber);

                // Stop at the next whitespace or special character
                int errorLength = findErrorEndOffset(remainingText);

                // Special case: highlight full remaining line if it's an import or syntax issue
                if (remainingText.trim().startsWith("import") || errorType.toLowerCase().contains("error")) {
                    errorLength = remainingText.length();
                }

                startOffset = lineStart + columnNumber;
                endOffset = Math.min(startOffset + errorLength, document.getTextLength());
            }

             TextAttributes ERROR_ATTRIBUTES = new TextAttributes(
                     new JBColor(
                             new Color(64, 16, 16, 120),
                             new Color(255, 235, 235)
                     ),
                     new JBColor(
                             new Color(255, 77, 77),
                             new Color(200, 50, 50)
                     ),
                    JBColor.RED,
                    EffectType.WAVE_UNDERSCORE,
                    Font.PLAIN
            );

             TextAttributes WARNING_ATTRIBUTES = new TextAttributes(
                     new JBColor(
                             new Color(55, 44, 20, 100),       
                             new Color(255, 250, 210)          
                     ),
                     new JBColor(
                             new Color(255, 180, 40),          
                             new Color(255, 140, 0)            
                     ),
                    JBColor.ORANGE,
                    EffectType.WAVE_UNDERSCORE,
                    Font.PLAIN
            );
            TextAttributes attributes = errorType.toLowerCase().contains("error") ? ERROR_ATTRIBUTES : WARNING_ATTRIBUTES;


            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                    startOffset, endOffset,
                    HighlighterLayer.ERROR,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE);

            highlighter.setErrorStripeTooltip(message);
            highlighter.setThinErrorStripeMark(true);
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

            highlighters.computeIfAbsent(file, k -> new ArrayList<>()).add(highlighter);
        });
    }
    private int findErrorEndOffset(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch) || ";,(){}[]".indexOf(ch) != -1) {
                return i;
            }
        }
        return text.length(); // If no break point found, highlight full
    }

    

    private void clearFixedHighlights() {
        Set<VirtualFile> oldFiles = new HashSet<>(highlighters.keySet());
        for (VirtualFile file : oldFiles) {
            if (!filesWithErrors.contains(file)) {
                Document doc = FileDocumentManager.getInstance().getDocument(file);
                if (doc != null) {
                    Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance()
                            .getEditors(doc, project);
                    for (Editor editor : editors) {
                        List<RangeHighlighter> list = highlighters.getOrDefault(file, List.of());
                        for (RangeHighlighter h : list) {
                            editor.getMarkupModel().removeHighlighter(h);
                        }
                    }
                }
                highlighters.remove(file);
            }
        }
        filesWithErrors.clear();
    }

    private void clearAllHighlights() {
        Map<VirtualFile, List<RangeHighlighter>> copy = new HashMap<>(highlighters);
        highlighters.clear();

        for (Map.Entry<VirtualFile, List<RangeHighlighter>> entry : copy.entrySet()) {
            VirtualFile file = entry.getKey();
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance()
                        .getEditors(document, project);
                for (Editor editor : editors) {
                    for (RangeHighlighter highlighter : entry.getValue()) {
                        try {
                            editor.getMarkupModel().removeHighlighter(highlighter);
                        } catch (Exception e) {
                            LOG.warn("Failed to remove highlighter", e);
                        }
                    }
                }
            }
        }
    }

    private VirtualFile findVirtualFile(String filePath) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return null;
        java.nio.file.Path resolvedPath = Paths.get(project.getBasePath()).resolve(filePath).normalize();
        return baseDir.getFileSystem().findFileByPath(resolvedPath.toString());
    }

    @Override
    public void dispose() {
        stop();
    }

    public void stop() {
        if (processHandler != null && !processHandler.isProcessTerminated()) {
            processHandler.destroyProcess();
            processHandler = null;
        }
        ApplicationManager.getApplication().invokeLater(this::clearAllHighlights);
    }

    public void startIfNeeded() {
        if (isCabalProject()) {
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

    public static GhcidRunner getInstance(Project project) {
        return project.getService(GhcidRunner.class);
    }

    public Project getProject() {
        return this.project;
    }
    public boolean isRunning() {
        return processHandler != null && !processHandler.isProcessTerminated();
    }

}
