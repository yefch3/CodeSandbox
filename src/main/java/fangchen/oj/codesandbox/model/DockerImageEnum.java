package fangchen.oj.codesandbox.model;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum DockerImageEnum {
    JAVA("java", "springci/spring-framwork-ci-jdk17:java_image");

    private final String text;
    private final String value;

    DockerImageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     */
    public static DockerImageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (DockerImageEnum anEnum : DockerImageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static String getValue(String text) {
        if (ObjectUtils.isEmpty(text)) {
            return null;
        }
        for (DockerImageEnum anEnum : DockerImageEnum.values()) {
            if (anEnum.text.equals(text)) {
                return anEnum.value;
            }
        }
        return null;
    }
}
