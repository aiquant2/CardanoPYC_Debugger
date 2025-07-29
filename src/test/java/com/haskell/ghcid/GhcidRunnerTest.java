
package com.haskell.ghcid;


import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;


public class GhcidRunnerTest extends BasePlatformTestCase {

    private GhcidRunner runner;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        runner = new GhcidRunner(getProject());
    }


    public void testIsCabalProject_withoutCabalProjectFile() {

        myFixture.getTempDirFixture().getTempDirPath();

        GhcidRunner runner = new GhcidRunner(getProject());
        assertFalse("Expected not to detect Cabal project", runner.isCabalProject());
    }


public void testIsCabalProject_withOnlyCabalProjectFreezeFile() {

    VirtualFile file = createTestFile("cabal.project.freeze", "frozen: true");

    GhcidRunner runner = new GhcidRunner(getProject());

    assertFalse("Expected not to detect Cabal project with only cabal.project.freeze", runner.isCabalProject());
}


public void testIsCabalProject_withoutDotCabalFile() {

    GhcidRunner runner = new GhcidRunner(getProject());

    assertFalse("Expected not to detect Cabal project without .cabal file", runner.isCabalProject());
}


    public void testIsCabalProject_withoutAnyCabalFiles() {
        createTestFile("Main.hs", "main = putStrLn \"Hello\"");
        GhcidRunner runner = new GhcidRunner(getProject());
        assertFalse(runner.isCabalProject());
    }

    @NotNull
    private VirtualFile createTestFile(@NotNull String name, @NotNull String content) {
        return myFixture.addFileToProject(name, content).getVirtualFile();
    }

    public void testIsNotCabalProject() {
        assertFalse("Should not detect Cabal project with no cabal files", runner.isCabalProject());
    }


    
}
