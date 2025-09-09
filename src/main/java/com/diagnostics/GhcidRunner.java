//
//package com.diagnostics;
//
//import com.intellij.execution.ExecutionException;
//import com.intellij.execution.configurations.GeneralCommandLine;
//import com.intellij.execution.process.ProcessEvent;
//import com.intellij.execution.process.ProcessAdapter;
//import com.intellij.execution.process.OSProcessHandler;
//import com.intellij.openapi.Disposable;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.editor.Document;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.editor.event.DocumentEvent;
//import com.intellij.openapi.editor.event.DocumentListener;
//import com.intellij.openapi.editor.markup.RangeHighlighter;
//import com.intellij.openapi.editor.markup.TextAttributes;
//import com.intellij.openapi.editor.markup.EffectType;
//import com.intellij.openapi.editor.markup.MarkupModel;
//import com.intellij.openapi.editor.markup.HighlighterLayer;
//import com.intellij.openapi.editor.markup.HighlighterTargetArea;
//import com.intellij.openapi.editor.markup.GutterIconRenderer;
//import com.intellij.openapi.fileEditor.FileDocumentManager;
//import com.intellij.openapi.fileEditor.FileEditorManager;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.Disposer;
//import com.intellij.openapi.util.Key;
//import com.intellij.openapi.vfs.LocalFileSystem;
//import com.intellij.openapi.vfs.VirtualFile;
//import com.intellij.ui.JBColor;
//import com.intellij.util.Alarm;
//import org.jetbrains.annotations.NotNull;
//import javax.swing.Icon;
//import java.awt.Color;
//import java.awt.Font;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.ArrayList;
//import java.util.Objects;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class GhcidRunner implements Disposable {
//    private static final Logger LOG = Logger.getInstance(GhcidRunner.class);
//    private final Project project;
//
//    private OSProcessHandler processHandler;
//    private final Map<VirtualFile, List<RangeHighlighter>> highlighters = new HashMap<>();
//    private final Set<VirtualFile> filesWithErrors = new HashSet<>();
//
//    private final Alarm restartAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
//    private boolean listenersAttached = false;
//
//    TextAttributes ERROR_ATTRIBUTES = new TextAttributes(
//            new JBColor(
//                    new Color(64, 16, 16, 120),
//                    new Color(255, 235, 235)
//            ),
//            new JBColor(
//                    new Color(255, 77, 77),
//                    new Color(200, 50, 50)
//            ),
//            JBColor.RED,
//            EffectType.WAVE_UNDERSCORE,
//            Font.PLAIN
//    );
//
//    TextAttributes WARNING_ATTRIBUTES = new TextAttributes(
//            new JBColor(
//                    new Color(55, 44, 20, 100),
//                    new Color(255, 250, 210)
//            ),
//            new JBColor(
//                    new Color(255, 180, 40),
//                    new Color(255, 140, 0)
//            ),
//            JBColor.ORANGE,
//            EffectType.WAVE_UNDERSCORE,
//            Font.PLAIN
//    );
//
//    public GhcidRunner(Project project) {
//        this.project = project;
//    }
//    private boolean isGhcidInstalled() {
//        try {
//            GeneralCommandLine checkCmd = new GeneralCommandLine()
//                    .withExePath("ghcid")
//                    .withParameters("--version");
//            OSProcessHandler checkHandler = new OSProcessHandler(checkCmd);
//            checkHandler.startNotify();
//            checkHandler.waitFor(2000); // wait max 2s
//            return checkHandler.getExitCode() == 0;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//
//    public void start() {
//        stop(); // cleanup old process
//        clearAllHighlights();
//        filesWithErrors.clear();
//
//        try {
//            GeneralCommandLine commandLine = new GeneralCommandLine()
//                    .withExePath("ghcid")
//                    .withParameters("--command", "cabal repl")
//                    .withWorkDirectory(project.getBasePath());
//
//            processHandler = new OSProcessHandler(commandLine);
//
//            Disposer.register(this, () -> {
//                if (processHandler != null && !processHandler.isProcessTerminated()) {
//                    processHandler.destroyProcess();
//                }
//            });
//
//            processHandler.addProcessListener(new ProcessAdapter() {
//                private final List<String> block = new ArrayList<>();
//
//                @Override
//                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
//                    String line = event.getText().trim();
//                    if (line.matches(".*\\.hs:\\d+:\\d+: (error|warning|\\[error]|\\[warning]):.*")) {
//                        if (!block.isEmpty()) {
//                            processGhcidOutputBlock(block.toArray(new String[0]));
//                            block.clear();
//                        }
//                    }
//                    if (!line.isEmpty()) {
//                        block.add(line);
//                    }
//                    if (line.isEmpty() && !block.isEmpty()) {
//                        processGhcidOutputBlock(block.toArray(new String[0]));
//                        block.clear();
//                    }
//                }
//
//                @Override
//                public void processTerminated(@NotNull ProcessEvent event) {
//                    if (event.getExitCode() == 0) {
//                        clearFixedHighlights();
//                    }
//                }
//            });
//
//            processHandler.startNotify();
//            attachDocumentListeners();
//        } catch (ExecutionException e) {
//            LOG.error("Failed to start ghcid", e);
//        }
//    }
//
//    private void attachDocumentListeners() {
//        if (listenersAttached) return;
//        listenersAttached = true;
//
//        Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getAllEditors();
//        for (Editor editor : editors) {
//            Document document = editor.getDocument();
//            document.addDocumentListener(new DocumentListener() {
//                @Override
//                public void documentChanged(@NotNull DocumentEvent event) {
//                    restartAlarm.cancelAllRequests();
//                    restartAlarm.addRequest(() -> {
//                        FileDocumentManager.getInstance().saveDocument(document);
//                        stop();
//                        start();
//                    }, 500); // debounce: 500 ms
//                }
//            }, this);
//        }
//    }
//
//    void processGhcidOutputBlock(String[] lines) {
//        if (lines == null || lines.length == 0) return;
//        Pattern pattern = Pattern.compile("^(?<file>.+?\\.hs):(?<line>\\d+):(?<column>\\d+):\\s*(?<type>error|warning|\\[error]|\\[warning]):?");
//        Matcher matcher = pattern.matcher(lines[0]);
//        if (!matcher.find()) return;
//
//        String filePath = matcher.group("file").trim();
//        int lineNumber = Integer.parseInt(matcher.group("line")) - 1;
//        int columnNumber = Integer.parseInt(matcher.group("column")) - 1;
//        String errorType = matcher.group("type");
//
//        StringBuilder messageBuilder = new StringBuilder();
//        for (String line : lines) {
//            messageBuilder.append(line).append("\n");
//        }
//
//        String fullMessage = messageBuilder.toString().trim();
//        VirtualFile file = findVirtualFile(filePath);
//        if (file != null) {
//            filesWithErrors.add(file);
//            highlightError(file, lineNumber, columnNumber, errorType, fullMessage);
//        } else {
//            LOG.warn("Could not find file: " + filePath);
//        }
//    }
//
//
//    protected void highlightError(VirtualFile file, int lineNumber, int columnNumber, String errorType, String message) {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            // Get document and validate
//            Document document = FileDocumentManager.getInstance().getDocument(file);
//            if (document == null) {
//                LOG.warn("Document is null for file: " + file.getPath());
//                return;
//            }
//
//            if (lineNumber < 0 || lineNumber >= document.getLineCount()) {
//                LOG.warn("Invalid line number: " + lineNumber);
//                return;
//            }
//
//            // Calculate error range
//            int lineStart = document.getLineStartOffset(lineNumber);
//            int lineEnd = document.getLineEndOffset(lineNumber);
//            String lineText = document.getText().substring(lineStart, lineEnd);
//            int startOffset = lineStart + Math.min(columnNumber, lineText.length());
//            String remainingText = lineText.substring(columnNumber);
//
//            // Special case: highlight full remaining line if it's an import or syntax issue
//            int errorLength;
//            if (remainingText.trim().startsWith("import") || errorType.toLowerCase().contains("error")) {
//                errorLength = remainingText.length();
//            } else {
//                errorLength = findErrorEndOffset(remainingText);
//            }
//            int endOffset = Math.min(startOffset + errorLength, document.getTextLength());
//
//            // Get or create editor
//            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
//            if (editor == null || !editor.getDocument().equals(document)) {
//                editor = FileEditorManager.getInstance(project).openTextEditor(
//                        new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, lineNumber, columnNumber), false);
//                if (editor == null) return;
//            }
//
//            // Remove any existing highlighters in this exact range
//            MarkupModel markupModel = editor.getMarkupModel();
//            for (RangeHighlighter existing : markupModel.getAllHighlighters()) {
//                if (existing.getStartOffset() == startOffset && existing.getEndOffset() == endOffset) {
//                    markupModel.removeHighlighter(existing);
//                }
//            }
//
//            // Create new highlighter
//            TextAttributes attributes = errorType.toLowerCase().contains("error") ? ERROR_ATTRIBUTES : WARNING_ATTRIBUTES;
//            RangeHighlighter hl = markupModel.addRangeHighlighter(
//                    startOffset, endOffset,
//                    HighlighterLayer.ERROR,
//                    attributes,
//                    HighlighterTargetArea.EXACT_RANGE);
//
//            // Configure highlighter
//            hl.setErrorStripeTooltip(message);
//            hl.setThinErrorStripeMark(true);
//            hl.setGutterIconRenderer(new MyGutterIconRenderer(errorType, message));
//
//            // Store the highlighter (clear old ones first)
//            highlighters.compute(file, (k, v) -> {
//                if (v != null) v.clear();
//                else v = new ArrayList<>();
//                v.add(hl);
//                return v;
//            });
//        });
//    }
//
//    private static class MyGutterIconRenderer extends GutterIconRenderer {
//        private final String errorType;
//        private final String message;
//
//        public MyGutterIconRenderer(String errorType, String message) {
//            this.errorType = errorType;
//            this.message = message;
//        }
//
//        @Override
//        public @NotNull Icon getIcon() {
//            return errorType.toLowerCase().contains("error")
//                    ? com.intellij.icons.AllIcons.General.Error
//                    : com.intellij.icons.AllIcons.General.Warning;
//        }
//
//        @Override
//        public String getTooltipText() {
//            return message;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) return true;
//            if (!(obj instanceof MyGutterIconRenderer)) return false;
//            MyGutterIconRenderer other = (MyGutterIconRenderer) obj;
//            return Objects.equals(errorType, other.errorType) &&
//                    Objects.equals(message, other.message);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(errorType, message);
//        }
//    }
//    private int findErrorEndOffset(String text) {
//        for (int i = 0; i < text.length(); i++) {
//            char ch = text.charAt(i);
//            if (Character.isWhitespace(ch) || ";,(){}[]".indexOf(ch) != -1) {
//                return i;
//            }
//        }
//        return text.length();
//    }
//
//    private void clearFixedHighlights() {
//        Set<VirtualFile> oldFiles = new HashSet<>(highlighters.keySet());
//        for (VirtualFile file : oldFiles) {
//            if (!filesWithErrors.contains(file)) {
//                Document doc = FileDocumentManager.getInstance().getDocument(file);
//                if (doc != null) {
//                    Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(doc, project);
//                    for (Editor editor : editors) {
//                        List<RangeHighlighter> list = highlighters.getOrDefault(file, List.of());
//                        for (RangeHighlighter h : list) {
//                            editor.getMarkupModel().removeHighlighter(h);
//                        }
//                    }
//                }
//                highlighters.remove(file);
//            }
//        }
//        filesWithErrors.clear();
//    }
//
//    private void clearAllHighlights() {
//        Map<VirtualFile, List<RangeHighlighter>> copy = new HashMap<>(highlighters);
//        highlighters.clear();
//
//        for (Map.Entry<VirtualFile, List<RangeHighlighter>> entry : copy.entrySet()) {
//            VirtualFile file = entry.getKey();
//            Document document = FileDocumentManager.getInstance().getDocument(file);
//            if (document != null) {
//                Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(document, project);
//                for (Editor editor : editors) {
//                    for (RangeHighlighter highlighter : entry.getValue()) {
//                        try {
//                            editor.getMarkupModel().removeHighlighter(highlighter);
//                        } catch (Exception e) {
//                            LOG.warn("Failed to remove highlighter", e);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
////    VirtualFile findVirtualFile(String filePath) {
////        VirtualFile baseDir = project.getBaseDir();
////        if (baseDir == null) return null;
////        Path resolvedPath = Paths.get(project.getBasePath()).resolve(filePath).normalize();
////        return baseDir.getFileSystem().findFileByPath(resolvedPath.toString());
////    }
//
//
//    VirtualFile findVirtualFile(String filePath) {
//        String basePath = project.getBasePath();
//        if (basePath == null) return null;
//
//        Path resolvedPath = Paths.get(basePath).resolve(filePath).normalize();
//        return LocalFileSystem.getInstance().findFileByPath(resolvedPath.toString());
//    }
//
//    @Override
//    public void dispose() {
//        stop();
//    }
//
//    public void stop() {
//        if (processHandler != null && !processHandler.isProcessTerminated()) {
//            processHandler.destroyProcess();
//            processHandler = null;
//        }
//        ApplicationManager.getApplication().invokeLater(this::clearAllHighlights);
//    }
//
//    public void startIfNeeded() {
//        if (isCabalProject()) {
//            start();
//        }
//    }
//
////    public boolean isCabalProject() {
////        VirtualFile projectDir = project.getBaseDir();
////        if (projectDir == null) return false;
////        return projectDir.findChild("cabal.project") != null ||
////                projectDir.findChild("cabal.project.freeze") != null ||
////                hasCabalFiles(projectDir);
////    }
////
////    public boolean isCabalProject() {
////        VirtualFile projectDir = project.getBaseDir();
////        if (projectDir == null) return false;
////
////        return containsCabalProjectFile(projectDir);
////    }
//public boolean isCabalProject() {
//    String basePath = project.getBasePath();
//    if (basePath == null) return false;
//
//    VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(basePath);
//    if (projectDir == null) return false;
//
//    return containsCabalProjectFile(projectDir);
//}
//
//    private boolean containsCabalProjectFile(VirtualFile dir) {
//        for (VirtualFile file : dir.getChildren()) {
//            if (!file.isDirectory()) {
//                String name = file.getName();
//                if (name.equals("cabal.project") || name.equals("cabal.project.freeze") || name.endsWith(".cabal")) {
//                    return true;
//                }
//            } else {
//                if (containsCabalProjectFile(file)) return true;
//            }
//        }
//        return false;
//    }
////
//
//    private boolean hasCabalFiles(VirtualFile dir) {
//        VirtualFile[] children = dir.getChildren();
//        for (VirtualFile file : children) {
//            if (file.isDirectory()) {
//                if (hasCabalFiles(file)) return true;
//            } else if (file.getName().endsWith(".cabal")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static GhcidRunner getInstance(Project project) {
//        return project.getService(GhcidRunner.class);
//    }
//
//    public Project getProject() {
//        return this.project;
//    }
//
//    public boolean isRunning() {
//        return processHandler != null && !processHandler.isProcessTerminated();
//    }
//}


package com.diagnostics;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Font;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GhcidRunner implements Disposable {
    private static final Logger LOG = Logger.getInstance(GhcidRunner.class);
    private final Project project;

    private OSProcessHandler processHandler;
    private final Map<VirtualFile, List<RangeHighlighter>> highlighters = new HashMap<>();
    private final Set<VirtualFile> filesWithErrors = new HashSet<>();

    private final Alarm restartAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
    private boolean listenersAttached = false;

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

    public GhcidRunner(Project project) {
        this.project = project;
    }

    private boolean isGhcidInstalled() {
        try {
            GeneralCommandLine checkCmd = new GeneralCommandLine()
                    .withExePath("ghcid")
                    .withParameters("--version");
            OSProcessHandler checkHandler = new OSProcessHandler(checkCmd);
            checkHandler.startNotify();
            checkHandler.waitFor(2000); // wait max 2s
            return checkHandler.getExitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ... existing code ...
    private com.intellij.notification.Notification currentNotification;

    public void start() {
        stop(); // cleanup old process
        clearAllHighlights();
        filesWithErrors.clear();

        // 🔍 Check if ghcid installed
        if (!isGhcidInstalled()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                // Expire any existing notification
                if (currentNotification != null && !currentNotification.isExpired()) {
                    currentNotification.expire();
                }

                currentNotification = com.intellij.notification.NotificationGroupManager.getInstance()
                        .getNotificationGroup("Ghcid Notifications")
                        .createNotification(
                                "Ghcid not found",
                                "Please install Ghcid (cabal install ghcid / stack install ghcid) for diagnostics.",
                                com.intellij.notification.NotificationType.ERROR
                        );
                currentNotification.notify(project);
            });
            return;
        }

        // Clear notification if ghcid is found
        if (currentNotification != null && !currentNotification.isExpired()) {
            currentNotification.expire();
            currentNotification = null;
        }


        try {
            GeneralCommandLine commandLine = new GeneralCommandLine()
                    .withExePath("ghcid")
                    .withParameters("--command", "cabal repl")
                    .withWorkDirectory(project.getBasePath());

            processHandler = new OSProcessHandler(commandLine);

            Disposer.register(this, () -> {
                if (processHandler != null && !processHandler.isProcessTerminated()) {
                    processHandler.destroyProcess();
                }
            });

            processHandler.addProcessListener(new ProcessAdapter() {
                private final List<String> block = new ArrayList<>();

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String line = event.getText().trim();
                    if (line.matches(".*\\.hs:\\d+:\\d+: (error|warning|\\[error]|\\[warning]):.*")) {
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
        if (listenersAttached) return;
        listenersAttached = true;

        Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getAllEditors();
        for (Editor editor : editors) {
            Document document = editor.getDocument();
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    restartAlarm.cancelAllRequests();
                    restartAlarm.addRequest(() -> {
                        FileDocumentManager.getInstance().saveDocument(document);
                        stop();
                        start();
                    }, 500); // debounce: 500 ms
                }
            }, this);
        }
    }

    void processGhcidOutputBlock(String[] lines) {
        if (lines == null || lines.length == 0) return;
        Pattern pattern = Pattern.compile("^(?<file>.+?\\.hs):(?<line>\\d+):(?<column>\\d+):\\s*(?<type>error|warning|\\[error]|\\[warning]):?");
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
            filesWithErrors.add(file);
            highlightError(file, lineNumber, columnNumber, errorType, fullMessage);
        } else {
            LOG.warn("Could not find file: " + filePath);
        }
    }

    protected void highlightError(VirtualFile file, int lineNumber, int columnNumber, String errorType, String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // Get document and validate
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                LOG.warn("Document is null for file: " + file.getPath());
                return;
            }

            if (lineNumber < 0 || lineNumber >= document.getLineCount()) {
                LOG.warn("Invalid line number: " + lineNumber);
                return;
            }

            // Calculate error range
            int lineStart = document.getLineStartOffset(lineNumber);
            int lineEnd = document.getLineEndOffset(lineNumber);
            String lineText = document.getText().substring(lineStart, lineEnd);
            int startOffset = lineStart + Math.min(columnNumber, lineText.length());
            String remainingText = lineText.substring(columnNumber);

            // Special case: highlight full remaining line if it's an import or syntax issue
            int errorLength;
            if (remainingText.trim().startsWith("import") || errorType.toLowerCase().contains("error")) {
                errorLength = remainingText.length();
            } else {
                errorLength = findErrorEndOffset(remainingText);
            }
            int endOffset = Math.min(startOffset + errorLength, document.getTextLength());

            // Get or create editor
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor == null || !editor.getDocument().equals(document)) {
                editor = FileEditorManager.getInstance(project).openTextEditor(
                        new com.intellij.openapi.fileEditor.OpenFileDescriptor(project, file, lineNumber, columnNumber), false);
                if (editor == null) return;
            }

            // Remove any existing highlighters in this exact range
            MarkupModel markupModel = editor.getMarkupModel();
            for (RangeHighlighter existing : markupModel.getAllHighlighters()) {
                if (existing.getStartOffset() == startOffset && existing.getEndOffset() == endOffset) {
                    markupModel.removeHighlighter(existing);
                }
            }

            // Create new highlighter
            TextAttributes attributes = errorType.toLowerCase().contains("error") ? ERROR_ATTRIBUTES : WARNING_ATTRIBUTES;
            RangeHighlighter hl = markupModel.addRangeHighlighter(
                    startOffset, endOffset,
                    HighlighterLayer.ERROR,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE);

            // Configure highlighter
            hl.setErrorStripeTooltip(message);
            hl.setThinErrorStripeMark(true);
            hl.setGutterIconRenderer(new MyGutterIconRenderer(errorType, message));

            // Store the highlighter (clear old ones first)
            highlighters.compute(file, (k, v) -> {
                if (v != null) v.clear();
                else v = new ArrayList<>();
                v.add(hl);
                return v;
            });
        });
    }

    private static class MyGutterIconRenderer extends GutterIconRenderer {
        private final String errorType;
        private final String message;

        public MyGutterIconRenderer(String errorType, String message) {
            this.errorType = errorType;
            this.message = message;
        }

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
            if (this == obj) return true;
            if (!(obj instanceof MyGutterIconRenderer)) return false;
            MyGutterIconRenderer other = (MyGutterIconRenderer) obj;
            return Objects.equals(errorType, other.errorType) &&
                    Objects.equals(message, other.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorType, message);
        }
    }

    private int findErrorEndOffset(String text) {
        System.out.println(text);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch) || ";,(){}[]".indexOf(ch) != -1) {
                return i;
            }
        }
        return text.length();
    }

    private void clearFixedHighlights() {
        Set<VirtualFile> oldFiles = new HashSet<>(highlighters.keySet());
        for (VirtualFile file : oldFiles) {
            if (!filesWithErrors.contains(file)) {
                Document doc = FileDocumentManager.getInstance().getDocument(file);
                if (doc != null) {
                    Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(doc, project);
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
                Editor[] editors = com.intellij.openapi.editor.EditorFactory.getInstance().getEditors(document, project);
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

    VirtualFile findVirtualFile(String filePath) {
        String basePath = project.getBasePath();
        if (basePath == null) return null;

        Path resolvedPath = Paths.get(basePath).resolve(filePath).normalize();
        return LocalFileSystem.getInstance().findFileByPath(resolvedPath.toString());
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
        String basePath = project.getBasePath();
        if (basePath == null) return false;

        VirtualFile projectDir = LocalFileSystem.getInstance().findFileByPath(basePath);
        if (projectDir == null) return false;

        return containsCabalProjectFile(projectDir);
    }

    private boolean containsCabalProjectFile(VirtualFile dir) {
        for (VirtualFile file : dir.getChildren()) {
            if (!file.isDirectory()) {
                String name = file.getName();
                if (name.equals("cabal.project") || name.equals("cabal.project.freeze") || name.endsWith(".cabal")) {
                    return true;
                }
            } else {
                if (containsCabalProjectFile(file)) return true;
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