package fangchen.oj.codesandbox;

import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

public interface CodeSandBox {
    // todo: 代码沙箱接口，目前实现了java的，后续可以实现cpp和python
    ExecuteCodeResponse executeCodeInteract(String code, List<String> inputList);
}
