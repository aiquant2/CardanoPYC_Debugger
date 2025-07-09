package com.pyc.haskell;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.editor.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GhcidRunner {

    public static class GhcidError {
        public final int offset;
        public final String message;

        public GhcidError(int offset, String message) {
            this.offset = offset;
            this.message = message;
        }
    }

    public static List<GhcidError> runGhcid(VirtualFile file, Project project) {
        List<GhcidError> errors = new ArrayList<>();

        String commandString = "ghcid --command=cabal repl " + file.getPath() + " --warnings";
        String[] command = {"/bin/sh", "-c", commandString};  // Execute full string with shell

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(project.getBasePath() != null ? project.getBasePath() : "."));
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Pattern regex = Pattern.compile("(.+):(\\d+):(\\d+): error: (.+)");

            while ((line = reader.readLine()) != null) {
                Matcher matcher = regex.matcher(line);
                if (matcher.matches()) {
                    String lineStr = matcher.group(2);
                    String colStr = matcher.group(3);
                    String message = matcher.group(4);

                    int lineNum = Integer.parseInt(lineStr);
                    int colNum = Integer.parseInt(colStr);

                    int offset = calculateOffset(project, file, lineNum, colNum);
                    errors.add(new GhcidError(offset, message.trim()));
                }
            }

        } catch (Exception e) {
            System.out.println("Error running ghcid: " + e.getMessage());
        }

        return errors;
    }

    private static int calculateOffset(Project project, VirtualFile file, int line, int column) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return 0;

        Document document = psiFile.getViewProvider().getDocument();
        if (document == null) return 0;

        int lineOffset = document.getLineStartOffset(line - 1);
        return lineOffset + (column - 1);
    }
}
