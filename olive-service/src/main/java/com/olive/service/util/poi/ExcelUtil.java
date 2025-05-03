package com.olive.service.util.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/// @author jhlz
/// @version 0.0.1
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

    // 获取单元格的值，处理合并单元格并避免 NPE
    private static String getCellValue(Sheet sheet, Cell cell) {
        if (cell == null) {
            return ""; // 如果单元格为空，直接返回空字符串
        }


        if (DateUtil.isCellDateFormatted(cell)) {

        }
        // 获取合并区域
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress range : mergedRegions) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                // 如果单元格在合并区域内，返回合并区域的起始单元格的值
                Row firstRow = sheet.getRow(range.getFirstRow());
                Cell firstCell = firstRow.getCell(range.getFirstColumn());
                return getCellStringValue(firstCell); // 获取起始单元格的值
            }
        }

        // 如果不是合并单元格，直接返回单元格的值
        return getCellStringValue(cell);
    }

    // 获取单元格的字符串值，避免 NPE
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return ""; // 如果单元格为空，返回空字符串
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return ""; // 如果单元格类型未知，返回空字符串
        }
    }

    record ExcelColumn<T>(String title, Function<T, ?> block){}
}
