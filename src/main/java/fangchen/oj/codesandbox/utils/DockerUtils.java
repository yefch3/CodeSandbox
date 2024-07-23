package fangchen.oj.codesandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import fangchen.oj.codesandbox.model.ContainerNameEnum;
import fangchen.oj.codesandbox.model.DockerImageEnum;
import fangchen.oj.codesandbox.model.ExecuteCmdMessage;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

// todo: 以后把这里面的配置写成文件然后读取
public class DockerUtils {

    private static DockerClient dockerClient;

    static {
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
            System.out.println("Failed to create docker client. " + e.getMessage());
        }
    }

    private static final Volume volume = new Volume("/judge");

    private static final Long DOCKER_MEMORY_LIMIT = 512 * 1024 * 1024L;

    public static String language;

    public static void createContainer() {
        try {
            String dockerImageId = DockerImageEnum.getValue(language);
            String dockerContainerName = ContainerNameEnum.getValue(language);
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withMemory(DOCKER_MEMORY_LIMIT)
                    .withMemorySwap(0L)
                    .withCpuCount(1L)
                    .withReadonlyRootfs(true)
                    .withBinds(new Bind("/home/ubuntu/fangchen/code-sandbox/testcode", volume, AccessMode.rw));

            assert dockerImageId != null;
            dockerClient.createContainerCmd(dockerImageId)
                    .withName(dockerContainerName)
                    .withHostConfig(hostConfig)
                    .withTty(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .withWorkingDir(volume.getPath())
                    .withNetworkDisabled(true)
                    .exec();
            System.out.println("Container created successfully");

            assert dockerContainerName != null;
            dockerClient.startContainerCmd(dockerContainerName).exec();
            System.out.println("Container started: " + dockerContainerName);
        } catch (Exception e) {
            System.out.println("Failed to create container. " + e.getMessage());
        }
    }


    public static void startContainer() {
        try {
            String dockerContainerName = ContainerNameEnum.getValue(language);
            assert dockerContainerName != null;
            dockerClient.startContainerCmd(dockerContainerName).exec();
            System.out.println("Container started: " + dockerContainerName);
        } catch (NotFoundException e) {
            System.out.println("Container not found. Creating a new one.");
            createContainer();
        } catch (Exception e) {
            System.out.println("The container has started. " + e.getMessage());
        }
    }


    public static ExecuteCmdMessage runCodeInDocker(String userCodeDir, String inputArgs) {
        String dockerContainerName = ContainerNameEnum.getValue(language);
        assert dockerContainerName != null;
        ExecCreateCmdResponse execCreateCmdResponse = generateRunCodeCmd(userCodeDir, inputArgs);

        ExecuteCmdMessage executeCmdMessage = new ExecuteCmdMessage();

        final long[] memory = {0L};

        StatsCmd statsCmd = dockerClient.statsCmd(dockerContainerName);
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
            @Override
            public void close() {

            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Statistics statistics) {
                MemoryStatsConfig memoryStats = statistics.getMemoryStats();
                long curMemory = memoryStats.getUsage() == null ? 0 : memoryStats.getUsage();
                memory[0] = Math.max(memory[0], curMemory);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
        statsCmd.exec(statisticsResultCallback);

        try {
            ResultCallback.Adapter<Frame> resultCallBack = getFrameAdapter(executeCmdMessage);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            assert execCreateCmdResponse != null;
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(resultCallBack)
                    .awaitCompletion(10000, TimeUnit.MILLISECONDS);
            stopWatch.stop();

            long time = stopWatch.getLastTaskTimeMillis();
            executeCmdMessage.setTime(time);

            executeCmdMessage.setMemory(memory[0]);
            statsCmd.close();

            System.out.println("Code executed finished");

        } catch (Exception e) {
            System.out.println("Failed to run code. " + e.getMessage());
        }
        return executeCmdMessage;
    }


    private static ResultCallback.Adapter<Frame> getFrameAdapter(ExecuteCmdMessage executeCmdMessage) {
        StringBuilder messageBuilder = new StringBuilder();
        return new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame item) {
                StreamType streamType = item.getStreamType();
                if (streamType == StreamType.STDOUT) {
                    System.out.println("Output: " + item);
                    executeCmdMessage.setExitValue(0);
                    messageBuilder.append(item.toString().replaceFirst("^(STDOUT:)\\s*", ""));
                } else if (streamType == StreamType.STDERR) {
                    System.out.println("Error: " + item);
                    executeCmdMessage.setExitValue(-1);
                    messageBuilder.append(item.toString().replaceFirst("^(STDERR:)\\s*", "")).append("\n");
                }
                super.onNext(item);
            }

            @Override
            public void onComplete() {
                executeCmdMessage.setMessage(messageBuilder.toString());
                super.onComplete();
            }
        };
    }


    private static ExecCreateCmdResponse generateRunCodeCmd(String userCodeDir, String inputArgs) {

        String dockerContainerName = ContainerNameEnum.getValue(language);
        assert dockerContainerName != null;
        switch (language) {
            case "java" -> {
                return dockerClient.execCreateCmd(dockerContainerName)
                        .withCmd("sh", "-c", "echo '" + inputArgs + "' | java -cp " + userCodeDir + " " + FileUtils.CODE_FILE_NAME)
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .exec();
            }
            default -> {
                return null;
            }
        }
    }

}
