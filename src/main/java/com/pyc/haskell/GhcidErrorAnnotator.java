package com.pyc.haskell;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GhcidErrorAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile == null) return;

        VirtualFile file = psiFile.getVirtualFile();
        if (file == null || !file.getName().endsWith(".hs")) return;

        List<GhcidRunner.GhcidError> errors = GhcidRunner.runGhcid(file, element.getProject());

        for (GhcidRunner.GhcidError error : errors) {
            if (element.getTextRange().contains(error.offset)) {
                holder.newAnnotation(HighlightSeverity.ERROR, error.message)
                        .range(element.getTextRange())
                        .create();
            }
        }
    }
}
