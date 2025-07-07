package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
//
//public class HaskellError {
//    private final String filePath;
//    private final int startLine;
//    private final int startColumn;
//    private final int endLine;
//    private final int endColumn;
//    private final String message;
//
//    public HaskellError(String filePath, int startLine, int startColumn, int endLine, int endColumn, String message) {
//        this.filePath = filePath;
//        this.startLine = startLine;
//        this.startColumn = startColumn;
//        this.endLine = endLine;
//        this.endColumn = endColumn;
//        this.message = message;
//    }
//
//    public VirtualFile getVirtualFile(Project project) {
//        return LocalFileSystem.getInstance().findFileByPath(filePath);
//    }
//
//    public int getStartOffset(Project project) {
//        VirtualFile file = getVirtualFile(project);
//        if (file == null) return 0;
//
//        var document = FileDocumentManager.getInstance().getDocument(file);
//        if (document == null || startLine < 1 || startLine > document.getLineCount()) return 0;
//
//        return document.getLineStartOffset(startLine - 1) + (startColumn - 1);
//    }
//
//    public int getEndOffset(Project project) {
//        VirtualFile file = getVirtualFile(project);
//        if (file == null) return 0;
//
//        var document = FileDocumentManager.getInstance().getDocument(file);
//        if (document == null || endLine < 1 || endLine > document.getLineCount()) return 0;
//
//        return document.getLineStartOffset(endLine - 1) + (endColumn - 1);
//    }
//
//    public String getMessage() {
//        return message;
//    }
//}
public class HaskellError {
    private final String message;
    private final String filePath;
    private final int line;
    private final int column;

    public HaskellError(String message, String filePath, int line, int column) {
        this.message = message;
        this.filePath = filePath;
        this.line = line;
        this.column = column;
    }

    public String getMessage() { return message; }

    public VirtualFile getVirtualFile(Project project) {
        return LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    public int getStartOffset(Project project) {
        VirtualFile vf = getVirtualFile(project);
        if (vf == null) return -1;
        Document doc = FileDocumentManager.getInstance().getDocument(vf);
        if (doc == null) return -1;
        return getLineColumnOffset(doc, line, column);
    }

    private int getLineColumnOffset(Document doc, int line, int col) {
        int offset = 0;
        try {
            offset = doc.getLineStartOffset(line - 1) + (column - 1);
        } catch (Exception ignored) {}
        return offset;
    }
}

