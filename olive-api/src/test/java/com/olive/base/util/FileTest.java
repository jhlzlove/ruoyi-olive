package com.olive.base.util;

import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author jhlz
 * @version x.x.x
 */
public class FileTest {
    /**
     * example:
     */
    @Test
    public void get_file_extension_test() {
        String filename = ".test.sql";
        System.out.println(filename.indexOf("."));
        // assertEquals(".sql", filename.substring(filename.indexOf('.')));
        assertTrue(filename.lastIndexOf(".") > 0);
        // System.out.println(filename.substring(filename.indexOf('.')));
    }

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
        System.out.println(Files.getFileExtension("http://hahha/ww/22/10/10/111.txt"));
    }
}
