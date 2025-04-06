package com.olive.framework.util.poi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class ExcelUtil<T> {

    List<ExcelColumn<T>> columns = new ArrayList<>();

    // response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    //             response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx");
    // // 告诉浏览器用什么软件可以打开此文件
    //         response.setHeader("content-Type", "application/octet-stream;charset=utf-8");
    // // 下载文件的默认名称
    //         response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "utf-8"));
    // outputStream = response.getOutputStream();

    public void addColumn(String title, Function<T, ?> block) {
        columns.add(new ExcelColumn<>(title, block));
    }

    public void render() {
        for (ExcelColumn<T> column : columns) {
            // System.out.println(column.block().apply());
        }
    }

    record ExcelColumn<T>(String title, Function<T, ?> block){}
}
