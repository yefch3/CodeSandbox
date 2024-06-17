package fangchen.oj.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeResult {
    /**
     * 判题信息，程序执行信息，消耗内存，消耗时间
     */

    // 这里指的是执行信息，只有三种情况，
    // 1. 编译成功且运行成功
    // 2. 编译成功但运行失败
    // 3. 编译失败
    private String message;

    // 这里指的是题目是否通过测试，要用到ProblemSubmitJudgeResultEnum
    private String result;

    private Long time;

    private Long memory;
}
