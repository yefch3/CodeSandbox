package fangchen.oj.codesandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CodeSandboxApplication {

    public static void main(String[] args) {

        SpringApplication.run(CodeSandboxApplication.class, args);

    }
}
