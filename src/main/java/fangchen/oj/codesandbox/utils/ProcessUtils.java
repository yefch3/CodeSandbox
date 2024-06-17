package fangchen.oj.codesandbox.utils;

import fangchen.oj.codesandbox.model.ExecuteCmdMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {
    public static ExecuteCmdMessage getInfo(Process process) {
        ExecuteCmdMessage executeCmdMessage = new ExecuteCmdMessage();
        try {
            executeCmdMessage.setExitValue(process.waitFor());
            int exitValue = process.waitFor();
            executeCmdMessage.setExitValue(exitValue);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder message = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                message.append(line);
            }
            executeCmdMessage.setMessage(message.toString());

            if (exitValue != 0) {
                StringBuilder error = new StringBuilder();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    error.append(line);
                }
                executeCmdMessage.setError(error.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Get process info failed", e);
        }
        return executeCmdMessage;
    }
}
