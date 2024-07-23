package fangchen.oj.codesandbox.service;

import fangchen.oj.codesandbox.model.ExecuteCodeRequest;
import fangchen.oj.codesandbox.model.ExecuteCodeResponse;

public interface RunCodeService {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
