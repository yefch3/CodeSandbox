package fangchen.oj.codesandbox.model;

import lombok.Data;

@Data
public class ExecuteCmdMessage {

    private int exitValue;

    private String message;

    private String error;
}
