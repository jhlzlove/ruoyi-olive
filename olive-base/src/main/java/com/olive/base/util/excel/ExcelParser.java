package com.olive.base.util.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * excel 解析工具
 *
 * @author jhlz
 * @version 0.0.1
 */
public class ExcelParser {

    /**
     * 解析数据
     * @param is excel 文件流
     * @return  结果集
     */
    public static List<Map<Integer, Object>> parseData(InputStream is) {
        // todo 超大文件使用 SXSSFWorkbook 优化
        try (Workbook workbook = WorkbookFactory.create(is)) {
            // 只取第一个 sheet
            Sheet sheet = workbook.getSheetAt(0);
            return getRows(sheet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取每一行的值赋给一个 map，组成 list
     * 如果想要省内存并提高效率可以将 map 换为 array
     *
     * @param sheet sheet
     * @return  结果集
     */
    public static List<Map<Integer, Object>> getRows(Sheet sheet) {
        List<Map<Integer, Object>> values = new ArrayList<>();
        for (Row row : sheet) {
            int numberOfCells = row.getPhysicalNumberOfCells();
            // String[] valueArr = new String[numberOfCells];
            Map<Integer, Object> map = new HashMap<>();
            for (int i = 0; i < numberOfCells; i++) {
                String value = mergeRegionValue(sheet, row.getCell(i));
                map.put(i, value);
                // valueArr[i] = value;
            }
            values.add(map);
        }
        return values;
    }

    /**
     * 读取合并区域的值
     *
     * @param sheet sheet
     * @param cell  cell
     * @return 字符串
     */
    private static String mergeRegionValue(Sheet sheet, Cell cell) {
        List<CellRangeAddress> regions = sheet.getMergedRegions();
        for (CellRangeAddress region : regions) {
            if (region.isInRange(cell)) {
                Row firstRow = sheet.getRow(region.getFirstRow());
                Cell firstCell = firstRow.getCell(region.getFirstColumn());
                return getCellValueAsString(firstCell);
            }
        }
        return getCellValueAsString(cell);
    }

    /**
     * 读取 cell 中的值
     *
     * @param cell cell
     * @return 字符串
     */
    private static String getCellValueAsString(Cell cell) {
        if (Objects.isNull(cell)) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell))
                    yield cell.getLocalDateTimeCellValue().toString();
                try {
                    Double cellValue = cell.getNumericCellValue();
                    yield String.valueOf(cellValue.longValue());
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
                // NumberToTextConverter.toText 可以获取精确的数字，如果不要求精度可以直接使用返回的 double，提高效率
                // String cellValue = NumberToTextConverter.toText(cell.getNumericCellValue());
                // BigDecimal num = new BigDecimal(cellValue);
            }
            case FORMULA -> cell.getCellFormula();
            case ERROR -> String.valueOf(cell.getErrorCellValue());
            case null, default -> null;
        };
    }
}
