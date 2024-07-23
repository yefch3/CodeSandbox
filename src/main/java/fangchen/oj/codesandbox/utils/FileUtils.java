package fangchen.oj.codesandbox.utils;

import cn.hutool.core.io.FileUtil;
import fangchen.oj.codesandbox.model.FileExtensionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.File;
import java.util.UUID;

@AllArgsConstructor
@Data
@Getter
public class FileUtils {

    public static final String CODE_DIR = "testcode";

    public static final String userDir = System.getProperty("user.dir");

    public static final String globalCodeDir = userDir + File.separator + CODE_DIR;

    public static final String CODE_FILE_NAME = "Main";

    public static String language;


    // 将代码保存为文件
    public static String saveCodeAsFile(String code) {
        if (!FileUtil.exist(globalCodeDir)) {
            FileUtil.mkdir(globalCodeDir);
        }

        String userCodeDir = String.valueOf(UUID.randomUUID());
        String absoluteUserCodeDir = globalCodeDir + File.separator + userCodeDir;
        String absoluteUserCodeFilePath = absoluteUserCodeDir + File.separator + CODE_FILE_NAME + FileExtensionEnum.getValue(language);

        FileUtil.writeString(code, absoluteUserCodeFilePath, "UTF-8");
        return userCodeDir;
    }

    // 删除用户的代码文件夹
    public static void cleanCode(String userCodeDir) {
        String absoluteUserCodeDir = globalCodeDir + File.separator + userCodeDir;
        if (FileUtil.exist(absoluteUserCodeDir)) {
            FileUtil.del(absoluteUserCodeDir);
            System.out.println("Code cleaned successfully");
        }
    }
}
