package fangchen.oj.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import fangchen.oj.codesandbox.CodeSandBox;
import fangchen.oj.codesandbox.model.*;
import fangchen.oj.codesandbox.utils.ProcessUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import org.apache.catalina.Host;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
//import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JavaDockerCodeSandbox implements CodeSandBox {

    private static DockerClient dockerClient;

    {
        DockerClient tempClient = null;
        try {
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(new URI("unix:///var/run/docker.sock"))
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();

            dockerClient = DockerClientBuilder.getInstance()
                    .withDockerHttpClient(httpClient)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();  // 处理异常，例如记录日志或抛出运行时异常
        }
//        dockerClient = tempClient;
    }


    private static final String DOCKER_CONTAINER_NAME = "judge_java_container";

    private static final String DOCKER_IMAGE_ID = "springci/spring-framwork-ci-jdk17:java_image";

    private static final Volume volume = new Volume("/judge");

    private static final String JAVA_FILE_EXTENSION = ".java";

    private static final String CODE_DIR = "testcode";

    private static final String CODE_FILE_NAME = "Main";

    private static final Long DOCKER_MEMORY_LIMIT = 100 * 1000 * 1000L;

    // 沙箱执行代码只返回代码执行结果，不返回题目是否通过测试
    // 1. 编译失败，返回编译失败的信息，时间，内存，输出列表均为空
    // 2. 编译成功，运行失败，返回运行失败的信息，时间，内存，输出列表均为到当前case的信息，后面的case不会执行
    // 3. 编译成功，运行成功，返回运行成功的信息，时间，内存，输出列表均为所有case的信息


    @Override
    public ExecuteCodeResponse executeCodeInteract(ExecuteCodeRequest executeCodeRequest) {
        String userCodeDir = saveCodeAsFile(executeCodeRequest);

        ExecuteCmdMessage compileCmdMessage = compileCode(userCodeDir);

        // 编译失败的情况
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if (compileCmdMessage.getExitValue() != 0) {
            executeCodeResponse.setExitValue(CmdMessageEnum.COMPILE_ERROR.getValue());
            executeCodeResponse.setMessage(compileCmdMessage.getMessage());
            executeCodeResponse.setTimeList(new ArrayList<>());
            executeCodeResponse.setMemoryList(new ArrayList<>());
            executeCodeResponse.setOutputList(new ArrayList<>());
            cleanCode(userCodeDir);
            return executeCodeResponse;
        }

//        createContainer();
        startContainer();
        uploadFileToContainer(userCodeDir + File.separator + CODE_FILE_NAME + ".class");
        List<String> outputList = new ArrayList<>();
        List<Long> timeList = new ArrayList<>();
        List<Long> memoryList = new ArrayList<>();
        for (String inputArgs : executeCodeRequest.getInputList()) {
            ExecuteCmdMessage executeCmdMessage = runCodeInDocker(volume.getPath(), inputArgs);
            if (executeCmdMessage.getExitValue() != 0) {
                executeCodeResponse.setExitValue(CmdMessageEnum.RUNTIME_ERROR.getValue());
                executeCodeResponse.setMessage(executeCmdMessage.getMessage());
                executeCodeResponse.setTimeList(timeList);
                executeCodeResponse.setMemoryList(memoryList);
                executeCodeResponse.setOutputList(outputList);
                cleanCode(userCodeDir);
                return executeCodeResponse;
            } else {
                outputList.add(executeCmdMessage.getMessage());
                timeList.add(executeCmdMessage.getTime());
                memoryList.add(executeCmdMessage.getMemory());
            }
        }

        executeCodeResponse.setExitValue(CmdMessageEnum.SUCCESS.getValue());
        executeCodeResponse.setMessage("Success");
        executeCodeResponse.setTimeList(timeList);
        executeCodeResponse.setMemoryList(memoryList);
        executeCodeResponse.setOutputList(outputList);
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



    // 删除用户的代码文件夹
    public void cleanCode(String userCodeDir) {
        if (FileUtil.exist(userCodeDir)) {
            FileUtil.del(userCodeDir);
        }
    }


    public void createContainer() {
        try {
            HostConfig hostConfig = HostConfig.newHostConfig()
                                    .withMemory(DOCKER_MEMORY_LIMIT);

            dockerClient.createContainerCmd(DOCKER_IMAGE_ID)
                                    .withName(DOCKER_CONTAINER_NAME)
                                    .withHostConfig(hostConfig)
                                    .withTty(true)
                                    .withAttachStderr(true)
                                    .withAttachStdout(true)
                                    .withAttachStdin(true)
                                    .withWorkingDir(volume.getPath())
                                    .exec();

            System.out.println("Container created successfully");

        } catch (Exception e) {
            System.out.println("Failed to create container. " + e.getMessage());
        }
    }


    public void startContainer() {
        // 如果已经启动，那么就跳过
//        if (dockerClient.inspectContainerCmd(DOCKER_CONTAINER_NAME).exec().getState().getRunning()) {
//            System.out.println("Container already started: " + DOCKER_CONTAINER_NAME);
//            return;
//        }
        try {
            dockerClient.startContainerCmd(DOCKER_CONTAINER_NAME).exec();
            System.out.println("Container started: " + DOCKER_CONTAINER_NAME);
        } catch (NotFoundException e) {
            System.out.println("Container not found. Creating a new one.");
            createContainer();
            startContainer();
        } catch (Exception e) {
            System.out.println("Failed to start the container. " + e.getMessage());
        }
    }


    public void uploadFileToContainer(String userCodePath) {
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(DOCKER_CONTAINER_NAME)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("mkdir", "-p", volume.getPath())
                    .exec();
            dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(new ExecStartResultCallback()).awaitCompletion();
            dockerClient.copyArchiveToContainerCmd(DOCKER_CONTAINER_NAME)
                    .withHostResource(userCodePath)
                    .withRemotePath(volume.getPath())
                    .exec();
            System.out.println("File uploaded successfully");
        } catch (Exception e) {
            System.out.println("Failed to upload file. " + e.getMessage());
        }
    }


    public void removeFileFromContainer(String userCodePath) {
        try {
            dockerClient.copyArchiveToContainerCmd(DOCKER_CONTAINER_NAME)
                    .withHostResource(userCodePath)
                    .withRemotePath(volume.getPath())
                    .exec();
            System.out.println("File uploaded successfully");
        } catch (Exception e) {
            System.out.println("Failed to upload file. " + e.getMessage());
        }
    }


    public ExecuteCmdMessage runCodeInDocker(String userCodeDir, String inputArgs) {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(DOCKER_CONTAINER_NAME)
                .withCmd("sh", "-c", "echo '" + inputArgs + "' | java Main")
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ExecuteCmdMessage executeCmdMessage = new ExecuteCmdMessage();

        try {
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame item) {
                    StreamType streamType = item.getStreamType();
                    if (streamType == StreamType.STDOUT) {
                        System.out.println("Output: " + item.toString());
                        executeCmdMessage.setExitValue(0);
                    } else if (streamType == StreamType.STDERR) {
                        System.out.println("Error: " + item.toString());
                        executeCmdMessage.setExitValue(-1);
                    }
                    executeCmdMessage.setMessage(item.toString());
                    super.onNext(item);
                }
            };
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(execStartResultCallback)
                    .awaitCompletion();
            System.out.println("Code executed finished");
        } catch (Exception e) {
            System.out.println("Failed to run code. " + e.getMessage());
        }
        return executeCmdMessage;
    }

}
