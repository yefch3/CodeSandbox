package fangchen.oj.codesandbox;

import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandBox {
    /**
     * Execute code in CodeSandBox
     * @return ExecuteCodeResponse
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

    ExecuteCodeResponse executeCodeInteract(ExecuteCodeRequest executeCodeRequest);
}
