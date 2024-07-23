//package fangchen.oj.codesandbox.impl;
//
//import cn.hutool.core.io.FileUtil;
//import fangchen.oj.codesandbox.CodeSandBox;
//import fangchen.oj.codesandbox.model.*;
//import fangchen.oj.codesandbox.utils.ProcessUtils;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Deprecated
//public class JavaNativeCodeSandbox implements CodeSandBox {
//
//    private static final String JAVA_FILE_EXTENSION = ".java";
//
//    private static final String CODE_DIR = "testcode";
//
//    private static final String CODE_FILE_NAME = "Main";
//
//    // 沙箱执行代码只返回代码执行结果，不返回题目是否通过测试
//    // 1. 编译失败，返回编译失败的信息，时间，内存，输出列表均为空
//    // 2. 编译成功，运行失败，返回运行失败的信息，时间，内存，输出列表均为到当前case的信息，后面的case不会执行
//    // 3. 编译成功，运行成功，返回运行成功的信息，时间，内存，输出列表均为所有case的信息
//    @Override
//    public ExecuteCodeResponse executeCodeInteract(ExecuteCodeRequest executeCodeRequest) {
//        String userCodeDir = saveCodeAsFile(executeCodeRequest);
//
//        ExecuteCmdMessage compileCmdMessage = compileCode(userCodeDir);
//
//        // 编译失败的情况
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        if (compileCmdMessage.getExitValue() != 0) {
//            executeCodeResponse.setExitValue(CmdMessageEnum.COMPILE_ERROR.getValue());
//            executeCodeResponse.setMessage(compileCmdMessage.getMessage());
//            executeCodeResponse.setTimeList(new ArrayList<>());
//            executeCodeResponse.setMemoryList(new ArrayList<>());
//            executeCodeResponse.setOutputList(new ArrayList<>());
//            cleanCode(userCodeDir);
//            return executeCodeResponse;
//        }
//
//        // 编译成功的情况，那就运行代码，如果有错误，将输出列表添加到返回响应中，并且直接返回，后面的case不会执行
//        List<String> inputList = executeCodeRequest.getInputList();
//        List<String> outputList = new ArrayList<>();
//        List<Long> timeList = new ArrayList<>();
//        List<Long> memoryList = new ArrayList<>();
//        int expectedInputSize = inputList.get(0).split(" ").length;
//        for (String inputArgs : inputList) {
//            ExecuteCmdMessage executeCmdMessage = runCodeInteract(userCodeDir, inputArgs, expectedInputSize);
//            // 运行失败
//            if (executeCmdMessage.getExitValue() != 0) {
//                executeCodeResponse.setExitValue(CmdMessageEnum.RUNTIME_ERROR.getValue());
//                executeCodeResponse.setMessage(executeCmdMessage.getMessage());
//                executeCodeResponse.setTimeList(timeList);
//                executeCodeResponse.setMemoryList(memoryList);
//                executeCodeResponse.setOutputList(outputList);
//                cleanCode(userCodeDir);
//                return executeCodeResponse;
//            }
//            outputList.add(executeCmdMessage.getMessage());
//            timeList.add(executeCmdMessage.getTime());
//            memoryList.add(executeCmdMessage.getMemory());
//        }
//
//        // 编译成功，运行成功的情况
//        cleanCode(userCodeDir);
//        executeCodeResponse.setExitValue(CmdMessageEnum.SUCCESS.getValue());
//        executeCodeResponse.setMessage("");
//        executeCodeResponse.setOutputList(outputList);
//        executeCodeResponse.setTimeList(timeList);
//        executeCodeResponse.setMemoryList(memoryList);
//
//        return executeCodeResponse;
//    }
//
//
//    // 将代码保存为文件
//    public String saveCodeAsFile(ExecuteCodeRequest executeCodeRequest) {
//        String code = executeCodeRequest.getCode();
//
//        String userDir = System.getProperty("user.dir");
//        String globalCodeDir = userDir + File.separator + CODE_DIR;
//
//        if (!FileUtil.exist(globalCodeDir)) {
//            FileUtil.mkdir(globalCodeDir);
//        }
//
//        String userCodeDir = globalCodeDir + File.separator + UUID.randomUUID();
//        String userCodeFilePath = userCodeDir + File.separator + CODE_FILE_NAME + JAVA_FILE_EXTENSION;
//
//        FileUtil.writeString(code, userCodeFilePath, "UTF-8");
//        return userCodeDir;
//    }
//
//
//    // 编译代码
//    public ExecuteCmdMessage compileCode(String userCodeDir) {
//        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeDir + File.separator + CODE_FILE_NAME + JAVA_FILE_EXTENSION);
//        // try里面是编译完成的情况，不管编译是否成功，都会执行；而catch里面是编译这个过程失败的情况
//        try {
//            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
//            return ProcessUtils.getInfo(compileProcess);
//        } catch (Exception e) {
//            throw new RuntimeException("Compile code failed", e);
//        }
//    }
//
//
//    public ExecuteCmdMessage runCodeInteract(String userCodeDir, String inputArgs, int expectedInputSize) {
//        String runCmd = String.format("java -cp %s %s", userCodeDir, CODE_FILE_NAME);
//        try {
//            Process runProcess = Runtime.getRuntime().exec(runCmd);
//            return ProcessUtils.getInfoInteract(runProcess, inputArgs, expectedInputSize);
//        } catch (Exception e) {
//            throw new RuntimeException("Run code failed", e);
//        }
//    }
//
//
//    // 删除用户的代码文件夹
//    public void cleanCode(String userCodeDir) {
//        if (FileUtil.exist(userCodeDir)) {
//            FileUtil.del(userCodeDir);
//        }
//    }
//
//
//    // 测试
//    public static void main(String[] args) {
//        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        String code = """
//                import java.util.Scanner;
//                               \s
//                public class Main {
//                    public static void main(String[] args) {
//                        // 创建Scanner对象用于从控制台读取输入
//                        Scanner scanner = new Scanner(System.in);
//                               \s
//                        // 提示用户输入第一个数
//                        int A = scanner.nextInt();
//                               \s
//                        // 提示用户输入第二个数
//                        int B = scanner.nextInt();
//                               \s
//                        // 计算A和B的和
//                        int sum = A + B;
//                               \s
//                        // 输出结果
//                       \s
//                        System.out.println(sum);
//                    }
//                }
//                               \s
//               \s""";
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setTimeLimit(1000L);
//        List<String> inputList = new ArrayList<>();
//        inputList.add("1 8");
//        inputList.add("2 9");
//        inputList.add("3 5");
//        executeCodeRequest.setInputList(inputList);
//        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCodeInteract(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//}
