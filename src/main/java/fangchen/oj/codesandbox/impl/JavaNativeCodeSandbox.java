package fangchen.oj.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import fangchen.oj.codesandbox.CodeSandBox;
import fangchen.oj.codesandbox.model.*;
import fangchen.oj.codesandbox.utils.ProcessUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandBox {

    private static final String JAVA_FILE_EXTENSION = ".java";

    private static final String CODE_DIR = "testcode";

    private static final String CODE_FILE_NAME = "Main";

    // 沙箱执行代码只返回代码执行结果，不返回题目是否通过测试，也就是说只对JudgeResult的message写入，不对result写入
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String userCodeDir = saveCodeAsFile(executeCodeRequest);

        JudgeResult judgeResult = compileCode(userCodeDir);

        // 编译失败的情况
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if (judgeResult.getResult().equals(ProblemSubmitJudgeResultEnum.COMPILE_ERROR.getValue())) {
            executeCodeResponse.setJudgeResult(judgeResult);
            cleanCode(userCodeDir);
            return executeCodeResponse;
        }

        // 编译成功的情况
        List<String> inputList = executeCodeRequest.getInputList();
        List<String> actualOutputList = runCode(userCodeDir, inputList);
        executeCodeResponse.setOutputList(actualOutputList);
        executeCodeResponse.setJudgeResult(judgeResult);

        cleanCode(userCodeDir);

        return executeCodeResponse;
    }

    public String saveCodeAsFile(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();

        String userDir = System.getProperty("user.dir");
        String globalCodeDir = userDir + File.separator + CODE_DIR;

        if (!FileUtil.exist(globalCodeDir)) {
            FileUtil.mkdir(globalCodeDir);
        }

        String userCodeDir = globalCodeDir + File.separator + UUID.randomUUID();
        String userCodeFilePath = userCodeDir + File.separator + CODE_FILE_NAME + JAVA_FILE_EXTENSION;

        FileUtil.writeString(code, userCodeFilePath, "UTF-8");
        return userCodeDir;
    }

    public JudgeResult compileCode(String userCodeDir) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeDir + File.separator + CODE_FILE_NAME + JAVA_FILE_EXTENSION);
        // try里面是编译完成的情况，不管编译是否成功，都会执行；而catch里面是编译这个过程失败的情况
        try {
            JudgeResult judgeResult = new JudgeResult();
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteCmdMessage executeCmdMessage = ProcessUtils.getInfo(compileProcess);
            judgeResult.setMessage(executeCmdMessage.getMessage());
            if (executeCmdMessage.getExitValue() == 0) {
                judgeResult.setResult(ProblemSubmitJudgeResultEnum.WAITING.getValue());
            } else {
                judgeResult.setResult(ProblemSubmitJudgeResultEnum.COMPILE_ERROR.getValue());
            }
            return judgeResult;
        } catch (Exception e) {
            throw new RuntimeException("Compile code failed", e);
        }
    }

    // 执行代码获取输出，返回的是输出的List
    public List<String> runCode(String userCodeDir, List<String> inputList) {
        List<String> outputList = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("java -cp %s %s %s", userCodeDir, CODE_FILE_NAME, input);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteCmdMessage executeCmdMessage = ProcessUtils.getInfo(runProcess);
                String output = executeCmdMessage.getMessage();
                if (executeCmdMessage.getExitValue() == 0) {
                    outputList.add(output);
                } else {
                    throw new RuntimeException("Run code failed: " + executeCmdMessage.getError());
                }
            } catch (Exception e) {
                throw new RuntimeException("Run code failed", e);
            }
        }
        return outputList;
    }

    // 这个方法是用来删除用户的代码文件夹的
    public void cleanCode(String userCodeDir) {
        FileUtil.del(userCodeDir);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        String code = """
                public class Main {
                    public static void main(String[] args) {
                        // 检查是否提供了两个参数
                        if (args.length != 2) {
                            System.out.println("Please provide exactly two numbers as arguments.");
                            return;
                        }

                        try {
                            // 将字符串参数转换为整数
                            int num1 = Integer.parseInt(args[0]);
                            int num2 = Integer.parseInt(args[1]);

                            // 计算和
                            int sum = num1 + num2;

                            // 输出结果
                            System.out.println(sum);
                        } catch (NumberFormatException e) {
                            // 捕获并处理可能的格式异常
                            System.out.println("Invalid number format. Please provide valid integers.");
                        }
                    }
                }
                """;
        executeCodeRequest.setCode(code);
        List<String> inputList = new ArrayList<>();
        inputList.add("1 2");
        inputList.add("3 4");
        executeCodeRequest.setInputList(inputList);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse.getOutputList().size());
        System.out.println(executeCodeResponse.getOutputList());
    }
}
