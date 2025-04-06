package com.olive.base.util;

import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

/**
 * @author jhlz
 * @version x.x.x
 */
public class FileTest {
    /**
     * example: file
     */
    @Test
    public void get_file_type_test() {
        String name = "http://haha.com.cn/22/11/22/text.json";
        System.out.println(FilenameUtils.getBaseName(name));
        System.out.println(FilenameUtils.getExtension(name));
        System.out.println(FilenameUtils.getName(name));
        System.out.println(FilenameUtils.getFullPath(name));

        String fileExtension = Files.getFileExtension("111.txt");
        System.out.println(fileExtension);
        System.out.println(Files.getNameWithoutExtension("http://hahha/ww/22/10/10/111.txt"));
    }
}
