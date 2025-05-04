package com.olive.base.util.excel;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Excel 导出辅助工具
 * @author jhlz
 * @version x.x.x
 */
public class ExcelRender<T> {

    public void addColumn(String title, Function<T, ?> function) {

    }

    public static void render(InputStream is, OutputStream os) {
        // response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        // response.addHeader("Param", "no-cache");
        // response.addHeader("Cache-Control", "no-cache");
        // String name = new String(file.getName().getBytes(Charset.forName("UTF-8")), "ISO8859-1");
        // response.addHeader("Content-Disposition", "attachment;filename=" + name);
    }

}
