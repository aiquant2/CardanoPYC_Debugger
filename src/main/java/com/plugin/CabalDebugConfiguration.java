


package com.plugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

public class CabalDebugConfiguration extends LocatableConfigurationBase<CommandLineState> {
    protected CabalDebugConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new CabalDebugSettingsEditor();
    }

    @NotNull
    @Override
    public CommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new CommandLineState(environment) {
            @NotNull
            @Override
            protected ProcessHandler startProcess() throws ExecutionException {
                String cabalRoot = CabalProjectDetector.getCabalRoot(getProject());
                if (cabalRoot == null) {
                    throw new ExecutionException("Not a Cabal project");
                }

                GeneralCommandLine commandLine = new GeneralCommandLine();
                commandLine.setWorkDirectory(cabalRoot);

                if (SystemInfo.isWindows) {
                    // Use cmd /c "cabal repl"
                    commandLine.setExePath("cmd");
                    commandLine.addParameters("/c", "cabal repl");
                } else {
                    // Unix-like: direct command
                    commandLine.setExePath("cabal");
                    commandLine.addParameters("repl");
                }

                return new OSProcessHandler(commandLine);
            }
        };
    }
}



//
//package com.yourplugin;
//
//import com.intellij.execution.ExecutionException;
//import com.intellij.execution.Executor;
//import com.intellij.execution.configurations.*;
//import com.intellij.execution.process.*;
//import com.intellij.execution.runners.ExecutionEnvironment;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.fileEditor.FileEditorManager;
//import com.intellij.testFramework.LightVirtualFile;
//import com.intellij.openapi.options.SettingsEditor;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.Key;
//import com.intellij.openapi.util.SystemInfo;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.OutputStreamWriter;
//import java.nio.charset.StandardCharsets;
//import java.util.regex.Pattern;
//
//public class CabalDebugConfiguration extends LocatableConfigurationBase<CommandLineState> {
//
//    protected CabalDebugConfiguration(Project project, ConfigurationFactory factory, String name) {
//        super(project, factory, name);
//    }
//
//    @NotNull
//    @Override
//    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
//        return new CabalDebugSettingsEditor();
//    }
//
//    @NotNull
//    @Override
//    public CommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
//        return new CommandLineState(environment) {
//            @NotNull
//            @Override
//            protected ProcessHandler startProcess() throws ExecutionException {
//                String cabalRoot = CabalProjectDetector.getCabalRoot(getProject());
//                if (cabalRoot == null) {
//                    throw new ExecutionException("Not a Cabal project");
//                }
//
//                Editor editor = FileEditorManager.getInstance(environment.getProject()).getSelectedTextEditor();
//                if (editor == null) throw new ExecutionException("No active editor");
//                String userCode = editor.getDocument().getText();
//
//                GeneralCommandLine commandLine = new GeneralCommandLine();
//                commandLine.setWorkDirectory(cabalRoot);
//
//                if (SystemInfo.isWindows) {
//                    commandLine.setExePath("cmd");
//                    commandLine.addParameters("/c", "cabal repl");
//                } else {
//                    commandLine.setExePath("cabal");
//                    commandLine.addParameters("repl");
//                }
//
//                OSProcessHandler handler = new OSProcessHandler(commandLine);
//                StringBuilder outputBuffer = new StringBuilder();
//
//                handler.addProcessListener(new ProcessAdapter() {
//                    boolean codeSent = false;
//
//                    @Override
//                    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
//                        String text = event.getText();
//                        outputBuffer.append(text);
//
//                        if (!codeSent && text.contains("Prelude>")) {
//                            codeSent = true;
//                            ApplicationManager.getApplication().executeOnPooledThread(() -> {
//                                try {
//                                    OutputStreamWriter stdin = new OutputStreamWriter(handler.getProcessInput(), StandardCharsets.UTF_8);
//                                    stdin.write(":set -XOverloadedStrings\n");
//                                    stdin.write(":module + Plutus.V2.Ledger.Api PlutusTx PlutusTx.Prelude\n");
//                                    stdin.write("import qualified Codec.Serialise as Serialise\n");
//                                    stdin.write("import qualified Data.ByteString.Lazy as LBS\n");
//                                    stdin.write(userCode + "\n");
//                                    stdin.write("LBS.putStrLn $ LBS.toLazyByteString $ Serialise.serialise val\n");
//                                    stdin.flush();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        }
//
//                        // Display .plutus content when CBOR appears
//                        if (Pattern.matches("^[0-9a-fA-F]+\\s*$", text.trim())) {
//                            String hex = text.trim();
//                            ApplicationManager.getApplication().invokeLater(() -> {
//                                LightVirtualFile file = new LightVirtualFile("validator.plutus", hex);
//                                FileEditorManager.getInstance(getProject()).openFile(file, true);
//                            });
//                        }
//                    }
//                });
//
//                return handler;
//            }
//        };
//    }
//}
