package com.pyc.cardanopyc_debugger.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;

import java.io.*;

public class RunPlutusScriptAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

        System.out.println("🔍 Right-click action triggered.");

        VirtualFile file = e.getDataContext().getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE);
        if (file == null || !file.getName().endsWith(".hs")) {
            Messages.showErrorDialog("Please select a .hs Haskell file", "Invalid File");
            return;
        }

        String filePath = file.getPath();

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "run_contract.sh", filePath);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            Messages.showInfoMessage("Plutus Output:\n" + output.toString(), "Plutus Validator Result");

        } catch (IOException ex) {
            Messages.showErrorDialog("Failed to run script:\n" + ex.getMessage(), "Execution Error");
        }
    }
}