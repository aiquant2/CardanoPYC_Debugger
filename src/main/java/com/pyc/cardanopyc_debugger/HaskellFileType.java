package com.pyc.cardanopyc_debugger;


import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellFileType extends LanguageFileType {
    public static final HaskellFileType INSTANCE = new HaskellFileType();

    private HaskellFileType() {
        super(HaskellLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Haskell File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Haskell source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "hs";
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}