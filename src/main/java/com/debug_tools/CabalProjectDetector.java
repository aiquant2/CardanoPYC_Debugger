package com.debug_tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;

public class CabalProjectDetector {
    private static final Logger LOG = Logger.getInstance(CabalProjectDetector.class);

    public static boolean isCabalProject(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            LOG.warn("Project base path is null");
            return false;
        }

        File[] cabalFiles = new File(basePath).listFiles((dir, name) ->
                name.endsWith(".cabal") || name.equals("cabal.project"));

        boolean found = cabalFiles != null && cabalFiles.length > 0;
        if (!found) {
            LOG.info("No Cabal files found in " + basePath);
        }
        return found;
    }

    public static String getCabalRoot(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            LOG.error("Project base path is null");
            return null;
        }

        File[] cabalFiles = new File(basePath).listFiles((dir, name) ->
                name.endsWith(".cabal") || name.equals("cabal.project"));

        if (cabalFiles == null || cabalFiles.length == 0) {
            LOG.warn("No Cabal files found in " + basePath);
            return null;
        }

        LOG.info("Found Cabal project at " + cabalFiles[0].getParent());
        return cabalFiles[0].getParent();
    }
}