
package com.diagnostics;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;


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


    public void testProcessGhcidOutputBlock_validErrorLine() {
        String[] lines = {
                "src/Main.hs:12:4: error: Variable not in scope: x",
                "    x + 1"
        };

        TestableGhcidRunner testRunner = new TestableGhcidRunner(getProject());
        testRunner.callProcessGhcidOutputBlock(lines);

        assertNotNull("Expected file to be found", testRunner.capturedFile);
        assertEquals(11, testRunner.capturedLine); // 12 - 1
        assertEquals(3, testRunner.capturedColumn); // 4 - 1
        assertEquals("error", testRunner.capturedErrorType);
        assertTrue(testRunner.capturedMessage.contains("Variable not in scope"));
    }


    private  class TestableGhcidRunner extends GhcidRunner {

        public VirtualFile capturedFile;
        public int capturedLine;
        public int capturedColumn;
        public String capturedErrorType;
        public String capturedMessage;

        public TestableGhcidRunner(@NotNull com.intellij.openapi.project.Project project) {
            super(project);
        }

        public void callProcessGhcidOutputBlock(String[] lines) {
            processGhcidOutputBlock(lines);
        }

       @Override
        protected void highlightError(VirtualFile file, int lineNumber, int columnNumber,
                                      String errorType, String fullMessage) {
            this.capturedFile = file;
            this.capturedLine = lineNumber;
            this.capturedColumn = columnNumber;
            this.capturedErrorType = errorType;
            this.capturedMessage = fullMessage;
        }

        @Override
        protected VirtualFile findVirtualFile(String path) {
            return createTestFile("src/Main.hs", "x + 1"); // simulate matching file
        }
    }
}
