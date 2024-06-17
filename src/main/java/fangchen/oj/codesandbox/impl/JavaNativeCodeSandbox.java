package fangchen.oj.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import fangchen.oj.codesandbox.CodeSandBox;
import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;
import fangchen.oj.codesandbox.model.JudgeResult;
import fangchen.oj.codesandbox.model.ProblemSubmitJudgeResultEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandBox {

    private static final String JAVA_FILE_EXTENSION = ".java";

    private static final String CODE_DIR = "testcode";

    private static final String CODE_FILE_NAME = "Main";

    // 沙箱执行代码只返回代码执行结果，不返回题目是否通过测试
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String userCodeFilePath = saveCodeAsFile(executeCodeRequest);

        JudgeResult judgeResult = compileCode(userCodeFilePath);

        // 编译失败的情况
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if (judgeResult.getResult().equals(ProblemSubmitJudgeResultEnum.COMPILE_ERROR.getValue())) {
            executeCodeResponse.setJudgeResult(judgeResult);
            return executeCodeResponse;
        }

        // 编译成功的情况
        List<String> actualOutputList = runCode(userCodeFilePath);
        executeCodeResponse.setOutputList(actualOutputList);
        executeCodeResponse.setJudgeResult(judgeResult);
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
        return userCodeFilePath;
    }

    public JudgeResult compileCode(String userCodeFilePath) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFilePath);
        // try里面是编译完成的情况，不管编译是否成功，都会执行；而catch里面是编译这个过程失败的情况
        try {
            JudgeResult judgeResult = new JudgeResult();
            StringBuilder compileResult = new StringBuilder();
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            int exitValue = compileProcess.waitFor();
            if (exitValue == 0) {
                BufferedReader compileReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                String line;
                while ((line = compileReader.readLine()) != null) {
                    compileResult.append(line);
                }
                judgeResult.setMessage(compileResult.toString());
                judgeResult.setResult(ProblemSubmitJudgeResultEnum.WAITING.getValue());
                return judgeResult;
            } else {
                BufferedReader compileErrorReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
                String line;
                while ((line = compileErrorReader.readLine()) != null) {
                    compileResult.append(line);
                }

                compileResult.append("\n");

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    compileResult.append(line);
                }
                judgeResult.setMessage(compileResult.toString());
                judgeResult.setResult(ProblemSubmitJudgeResultEnum.COMPILE_ERROR.getValue());
                return judgeResult;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Compile code failed", e);
        }
    }

    public List<String> runCode(String userCodeFilePath) {
        return null;
    }
}
