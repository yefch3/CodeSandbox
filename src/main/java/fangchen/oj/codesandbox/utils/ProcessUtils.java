package fangchen.oj.codesandbox.utils;

import cn.hutool.core.util.StrUtil;
import fangchen.oj.codesandbox.model.ExecuteCmdMessage;

import java.io.*;

public class ProcessUtils {

    // 执行命令获取执行信息，可以用于编译代码和运行代码
    public static ExecuteCmdMessage getInfo(Process process) {
        ExecuteCmdMessage executeCmdMessage = new ExecuteCmdMessage();
        try {
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
                executeCmdMessage.setMessage(error.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Get process info failed", e);
        }
        return executeCmdMessage;
    }


    // 交互式输入，仅用于运行代码
    // todo: 对于某些可能引发错误对输入，需要特殊处理，比如输入两个数，如果只输入了一个数，那么程序会一直等待第二个数，这时候就需要特殊处理
    // 方法一： 超时机制
    // 方法二： 读取样例输入，样例输入的size应该和用户输入的size一致，如果不一致，那么就直接返回错误
    public static ExecuteCmdMessage getInfoInteract(Process process, String input) {
        ExecuteCmdMessage executeCmdMessage = new ExecuteCmdMessage();
        try {
            InputStream inputStream = process.getInputStream();
            OutputStream outputStream = process.getOutputStream();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] Args = input.split(" ");
            outputStreamWriter.write(StrUtil.join("\n", (Object) Args) + "\n");
            outputStreamWriter.flush();

            int exitValue = process.waitFor();
            executeCmdMessage.setExitValue(exitValue);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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
                executeCmdMessage.setMessage(error.toString());
                outputStreamWriter.close();
                outputStream.close();
                inputStream.close();
                process.destroy();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Get process info failed", e);
        }
        return executeCmdMessage;
    }
}
