package com.debug_tools;

import com.intellij.execution.configurations.GeneralCommandLine;
import org.junit.Test;

import static org.junit.Assert.*;

public class CabalDebugConfigurationTest {

    @Test
    public void testBuildCommandLine_unix() {
        String dummyCabalRoot = "/home/user/project";

        GeneralCommandLine cmd = CabalDebugConfiguration.buildCommandLine(dummyCabalRoot, false);

        assertEquals("/home/user/project", cmd.getWorkDirectory().getAbsolutePath());
        assertEquals("/bin/bash", cmd.getExePath());
        assertTrue(cmd.getCommandLineString().contains("echo"));
        assertTrue(cmd.getCommandLineString().contains("cabal repl"));
    }
}
