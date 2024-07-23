package fangchen.oj.codesandbox.service.impl;

import fangchen.oj.codesandbox.impl.JavaDockerCodeSandbox;
import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;
import fangchen.oj.codesandbox.service.RunCodeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunCodeServiceImpl implements RunCodeService {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        return switch (language) {
            case "java" -> {
                JavaDockerCodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
                yield javaDockerCodeSandbox.executeCodeInteract(code, inputList);
            }
            default -> null;
        };
    }
}
