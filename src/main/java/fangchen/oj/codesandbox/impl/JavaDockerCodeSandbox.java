package fangchen.oj.codesandbox.impl;

import fangchen.oj.codesandbox.CodeSandBox;
import fangchen.oj.codesandbox.model.*;
import fangchen.oj.codesandbox.utils.DockerUtils;
import fangchen.oj.codesandbox.utils.FileUtils;
import fangchen.oj.codesandbox.utils.ProcessUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JavaDockerCodeSandbox implements CodeSandBox {

    private static final String language = "java";
    // 沙箱执行代码只返回代码执行结果，不返回题目是否通过测试
    // 1. 编译失败，返回编译失败的信息，时间，内存，输出列表均为空
    // 2. 编译成功，运行失败，返回运行失败的信息，时间，内存，输出列表均为到当前case的信息，后面的case不会执行
    // 3. 编译成功，运行成功，返回运行成功的信息，时间，内存，输出列表均为所有case的信息
    @Override
    public ExecuteCodeResponse executeCodeInteract(String code, List<String> inputList) {
        FileUtils.language = language;
        String userCodeDir = FileUtils.saveCodeAsFile(code);

        String globalUserCodeDir = FileUtils.globalCodeDir + File.separator + userCodeDir;
        ExecuteCmdMessage compileCmdMessage = compileCode(globalUserCodeDir, FileExtensionEnum.getValue(language));

        // 编译失败的情况
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if (compileCmdMessage.getExitValue() != 0) {
            executeCodeResponse.setExitValue(CmdMessageEnum.COMPILE_ERROR.getValue());
            executeCodeResponse.setMessage(compileCmdMessage.getMessage());
            executeCodeResponse.setTimeList(new ArrayList<>());
            executeCodeResponse.setMemoryList(new ArrayList<>());
            executeCodeResponse.setOutputList(new ArrayList<>());
            FileUtils.cleanCode(userCodeDir);
            return executeCodeResponse;
        }
        System.out.println("Compile success");

        try {
            DockerUtils.language = language;
            DockerUtils.startContainer();
            List<String> outputList = new ArrayList<>();
            List<Long> timeList = new ArrayList<>();
            List<Long> memoryList = new ArrayList<>();
            for (String inputArgs : inputList) {
                ExecuteCmdMessage executeCmdMessage = DockerUtils.runCodeInDocker(userCodeDir, inputArgs);
                if (executeCmdMessage.getExitValue() != 0) {
                    executeCodeResponse.setExitValue(CmdMessageEnum.RUNTIME_ERROR.getValue());
                    executeCodeResponse.setMessage(executeCmdMessage.getMessage());
                    executeCodeResponse.setTimeList(timeList);
                    executeCodeResponse.setMemoryList(memoryList);
                    executeCodeResponse.setOutputList(outputList);
                    FileUtils.cleanCode(userCodeDir);
                    return executeCodeResponse;
                } else {
                    outputList.add(executeCmdMessage.getMessage());
                    timeList.add(executeCmdMessage.getTime());
                    memoryList.add(executeCmdMessage.getMemory());
                }
            }

            FileUtils.cleanCode(userCodeDir);
            executeCodeResponse.setExitValue(CmdMessageEnum.SUCCESS.getValue());
            executeCodeResponse.setMessage("Success");
            executeCodeResponse.setTimeList(timeList);
            executeCodeResponse.setMemoryList(memoryList);
            executeCodeResponse.setOutputList(outputList);
            return executeCodeResponse;
        } catch (Exception e) {
            FileUtils.cleanCode(userCodeDir);
            throw new RuntimeException("Failed to execute code", e);
        }
    }


    // 编译代码
    public ExecuteCmdMessage compileCode(String userCodeDir, String FILE_EXTENSION) {
        System.out.println("Start to compile code");
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeDir + File.separator + FileUtils.CODE_FILE_NAME + FILE_EXTENSION);
        // try里面是编译完成的情况，不管编译是否成功，都会执行；而catch里面是编译这个过程失败的情况
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            return ProcessUtils.getInfo(compileProcess);
        } catch (Exception e) {
            throw new RuntimeException("Compile code failed", e);
        }
    }

}
