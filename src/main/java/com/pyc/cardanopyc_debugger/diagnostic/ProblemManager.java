package com.pyc.cardanopyc_debugger.diagnostic;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.editor.Document;

import com.intellij.openapi.editor.markup.TextAttributes;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.awt.*;
import java.util.List;

public class ProblemManager {
    private final Project project;
    private static final TextAttributes ERROR_ATTRIBUTES = new TextAttributes();

    static {
        ERROR_ATTRIBUTES.setBackgroundColor(new Color(255, 0, 0, 30));
        ERROR_ATTRIBUTES.setErrorStripeColor(new Color(255, 0, 0));
        ERROR_ATTRIBUTES.setEffectColor(Color.RED);
        ERROR_ATTRIBUTES.setEffectType(EffectType.WAVE_UNDERSCORE);
    }

    public ProblemManager(Project project) {
        this.project = project;
    }

    public static ProblemManager getInstance(Project project) {
        return project.getService(ProblemManager.class);
    }
//
//    public void showProblems(List<HaskellError> errors) {
//        clearProblems();
//
//        for (HaskellError error : errors) {
//            VirtualFile vfile = error.getVirtualFile(project);
//            Editor[] editors = EditorFactory.getInstance().getEditors(FileDocumentManager.getInstance().getDocument(vfile), project);
//            if (editors.length > 0) {
//                Editor editor = editors[0];
//                MarkupModel markupModel = editor.getMarkupModel();
//                markupModel.addRangeHighlighter(
//                        error.getStartOffset(project),
//                        error.getEndOffset(project),
//                        HighlighterLayer.ERROR,
//                        ERROR_ATTRIBUTES,
//                        HighlighterTargetArea.EXACT_RANGE
//                );
//
//            }
//        }
//    }


//    public void showProblems(List<HaskellError> errors) {
//        clearProblems();
//        for (HaskellError error : errors) {
//            VirtualFile file = error.getVirtualFile(project);
//            if (file == null) continue;
//
//            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
//            if (psiFile == null) continue;
//
//            Document document = FileDocumentManager.getInstance().getDocument(file);
//            if (document == null) continue;
//
//            int offset = error.getStartOffset(project);
//            if (offset < 0 || offset >= document.getTextLength()) continue;
//
//            HighlightInfo info = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR)
//                    .range(offset, offset + 1)
//                    .descriptionAndTooltip(error.getMessage())
//                    .create();
//
//            DaemonCodeAnalyzer.getInstance(project).restart(psiFile); // Ensure analyzer runs
//            Editor[] editors = EditorFactory.getInstance().getEditors(document, project);
//            for (Editor editor : editors) {
//                if (info != null) {
//                    editor.getMarkupModel().addRangeHighlighter(
//                            offset, offset + 1, 5000, info.getTextAttributes(project), null
//                    );
//                }
//            }
//        }
//    }
public void showProblems(List<HaskellError> errors) {
    clearProblems();

    TextAttributes errorAttributes = new TextAttributes();
    errorAttributes.setEffectColor(Color.RED);
    errorAttributes.setEffectType(com.intellij.openapi.editor.markup.EffectType.WAVE_UNDERSCORE);
    errorAttributes.setForegroundColor(Color.RED);

    for (HaskellError error : errors) {
        VirtualFile file = error.getVirtualFile(project);
        if (file == null) continue;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) continue;

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) continue;

        int offset = error.getStartOffset(project);
        if (offset < 0 || offset >= document.getTextLength()) continue;

        DaemonCodeAnalyzer.getInstance(project).restart(psiFile);

        Editor[] editors = EditorFactory.getInstance().getEditors(document, project);
        for (Editor editor : editors) {
            editor.getMarkupModel().addRangeHighlighter(
                    offset, offset + 1, 5000, errorAttributes, null
            );
        }
    }
}

    public void clearProblems() {
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            editor.getMarkupModel().removeAllHighlighters();
        }
    }

    public void updateErrorDecorations() {
        // Optional: future improvements to update visible errors only
    }
}
