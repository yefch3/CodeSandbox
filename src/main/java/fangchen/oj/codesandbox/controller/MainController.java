package fangchen.oj.codesandbox.controller;

import fangchen.oj.codesandbox.impl.JavaDockerCodeSandbox;
import fangchen.oj.codesandbox.impl.JavaNativeCodeSandbox;
import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Fangchen Ye!!!!!";
    }

    @GetMapping("/test")
    public ExecuteCodeResponse test() {
        JavaDockerCodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        String code = """
                import java.util.Scanner;
                               \s
                public class Main {
                    public static void main(String[] args) {
                        // 创建Scanner对象用于从控制台读取输入
                        Scanner scanner = new Scanner(System.in);
                               \s
                        // 提示用户输入第一个数
                        int A = scanner.nextInt();
                               \s
                        // 提示用户输入第二个数
                        int B = scanner.nextInt();
                               \s
                        System.out.println(A + B);
                    }
                }
                               \s
               \s""";
        executeCodeRequest.setCode(code);
        executeCodeRequest.setTimeLimit(1000L);
        List<String> inputList = new ArrayList<>();
        inputList.add("1010 1100");
        inputList.add("512 79428");
        inputList.add("3 7");
        executeCodeRequest.setInputList(inputList);
        return javaDockerCodeSandbox.executeCodeInteract(executeCodeRequest);
    }
}
