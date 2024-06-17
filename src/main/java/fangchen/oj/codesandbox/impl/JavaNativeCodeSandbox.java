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
    // 1. 编译失败，那么message就是编译失败的信息，result就是compile error
    // 2. 编译成功，运行失败，那么message就是运行失败的信息，result就是wrong answer
    // 3. 编译成功，运行成功，那么message空，result就是waiting，等待判题
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String userCodeDir = saveCodeAsFile(executeCodeRequest);

        ExecuteCmdMessage compileCmdMessage = compileCode(userCodeDir);
        JudgeResult judgeResult = new JudgeResult(null, null, 0L, 0L);

        // 编译失败的情况
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if (compileCmdMessage.getExitValue() != 0) {
            judgeResult.setMessage(compileCmdMessage.getMessage());
            judgeResult.setResult(ProblemSubmitJudgeResultEnum.COMPILE_ERROR.getValue());
            executeCodeResponse.setJudgeResult(judgeResult);
            cleanCode(userCodeDir);
            return executeCodeResponse;
        }

        // 编译成功的情况，那就运行代码，如果有错误，将输出列表添加到返回响应中，并且直接返回，后面的case不会执行
        List<String> inputList = executeCodeRequest.getInputList();
        List<String> outputList = new ArrayList<>();
        judgeResult.setResult(ProblemSubmitJudgeResultEnum.WAITING.getValue());
        for (String inputArgs : inputList) {
            ExecuteCmdMessage executeCmdMessage = runCode(userCodeDir, inputArgs);
            if (executeCmdMessage.getExitValue() != 0) {
                judgeResult.setMessage(executeCmdMessage.getMessage());
                judgeResult.setResult(ProblemSubmitJudgeResultEnum.WRONG_ANSWER.getValue());
                executeCodeResponse.setJudgeResult(judgeResult);
                executeCodeResponse.setOutputList(outputList);
                cleanCode(userCodeDir);
                return executeCodeResponse;
            }
            outputList.add(executeCmdMessage.getMessage());
        }

        // 编译成功，运行成功的情况
        cleanCode(userCodeDir);
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setJudgeResult(judgeResult);

        return executeCodeResponse;
    }


    // 将代码保存为文件
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


    // 编译代码
    public ExecuteCmdMessage compileCode(String userCodeDir) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeDir + File.separator + CODE_FILE_NAME + JAVA_FILE_EXTENSION);
        // try里面是编译完成的情况，不管编译是否成功，都会执行；而catch里面是编译这个过程失败的情况
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            return ProcessUtils.getInfo(compileProcess);
        } catch (Exception e) {
            throw new RuntimeException("Compile code failed", e);
        }
    }


    // 执行代码获取一组输出
    public ExecuteCmdMessage runCode(String userCodeDir, String inputArgs) {
        String runCmd = String.format("java -cp %s %s %s", userCodeDir, CODE_FILE_NAME, inputArgs);
        try {
            Process runProcess = Runtime.getRuntime().exec(runCmd);
            //            System.out.println(executeCmdMessage);
            return ProcessUtils.getInfo(runProcess);
        } catch (Exception e) {
            throw new RuntimeException("Run code failed", e);
        }
    }


    // 删除用户的代码文件夹
    public void cleanCode(String userCodeDir) {
        FileUtil.del(userCodeDir);
    }


    // 测试
    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        String code = """
                public class Main {
                    public static void main(String[] args) {
                        // 检查是否提供了两个参数

                        // 将字符串参数转换为整数
                        int num1 = Integer.parseInt(args[0]);
                        int num2 = Integer.parseInt(args[1]);

                        // 计算和
                        int sum = num1 + num2;

                        // 输出结果
                        System.out.println(sum);
                    }
                }
                """;
        executeCodeRequest.setCode(code);
        List<String> inputList = new ArrayList<>();
        inputList.add("1 2");
        inputList.add("2 3");
        inputList.add("3");
        executeCodeRequest.setInputList(inputList);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }
}
