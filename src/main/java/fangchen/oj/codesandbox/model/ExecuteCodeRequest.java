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
public class ExecuteCodeRequest {

    private List<String> inputList;

    private String code;

    private String language;

    private Long timeLimit;

    private Long memoryLimit;
}
