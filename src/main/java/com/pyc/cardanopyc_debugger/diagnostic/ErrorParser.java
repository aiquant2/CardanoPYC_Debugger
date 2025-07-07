package com.pyc.cardanopyc_debugger.diagnostic;

import java.util.ArrayList;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorParser {
    public static List<HaskellError> parseGhcidOutput(String line) {
        List<HaskellError> errors = new ArrayList<>();
        // Example: /path/Main.hs:10:5: error: Variable not in scope
        Pattern pattern = Pattern.compile("(.+\\.hs):(\\d+):(\\d+):(?:\\s*(\\d+):(\\d+):)?\\s*error:(.*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String path = matcher.group(1).trim();
            int startLine = Integer.parseInt(matcher.group(2));
            int startCol = Integer.parseInt(matcher.group(3));
            int endLine = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : startLine;
            int endCol = matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) : startCol + 1;
            String msg = matcher.group(6).trim();
            errors.add(new HaskellError(msg,path, startLine, startCol));
        }
        return errors;
    }
}

