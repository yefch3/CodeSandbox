package fangchen.oj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteCodeResponse {
    private List<String> outputList;

    private Integer exitValue;

    private String message;

    // 使用List存储每个case的时间和内存，方便比较在哪一个case超时或者内存超限

    private List<Long> timeList;

    private List<Long> memoryList;
}
