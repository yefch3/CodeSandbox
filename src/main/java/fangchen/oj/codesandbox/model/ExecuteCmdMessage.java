package fangchen.oj.codesandbox.model;

import lombok.Data;

@Data
public class ExecuteCmdMessage {

    private Integer exitValue;

    private String message;

    private Long time;

    private Long memory;
}
