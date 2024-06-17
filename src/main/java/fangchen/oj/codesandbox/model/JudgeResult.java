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

    // 这里指的是执行信息，比如是否成功运行，编译错误等，和题目是否通过测试无关
    private String message;

    // 这里指的是题目是否通过测试，要用到ProblemSubmitJudgeResultEnum
    private String result;

    private Long time;

    private Long memory;
}
