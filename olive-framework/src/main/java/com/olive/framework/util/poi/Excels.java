package com.olive.framework.util.poi;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author jhlz
 * @version 1.0.0
 */
public class Excels {
    public static final int ROWS = 5000;

    /**
     * 创建工作簿
     *
     * @param fileName
     * @return
     */
    public static Workbook createWorkbook(String fileName) {
        org.springframework.util.StopWatch stopWatch = new org.springframework.util.StopWatch();
        stopWatch.start();


        return new HSSFWorkbook();
    }

    public final static String SUFFIX = ".xlsx";
    private final static int ROWS_CHANGE = 5000;
    public static final Logger logger = LoggerFactory.getLogger(Excels.class);

    /**
     * 创建excel
     *
     * @param sheetMap sheet映射, sheetEn为键
     * @param dataMap  数据映射, sheetEn为键
     * @param titleMap 数据表头映射, sheetEn为键
     * @param <T>      数据泛型
     * @return
     */
    public static <T extends Map<String, Object>> Workbook createExcel(Map<String, String> sheetMap, Map<String, List<T>> dataMap, Map<String, List<Map<String, String>>> titleMap) {
        logger.info("开始生成Excel文件，sheet数量为[{}]", sheetMap.size());
        StopWatch stopWatch = StopWatch.createStarted();
        if (Objects.isNull(sheetMap) || Objects.isNull(dataMap) || Objects.isNull(titleMap)) {
            throw new java.lang.RuntimeException("数据为空, 不能导出");
        }
        if (sheetMap.size() < 1) {
            throw new java.lang.RuntimeException("数据为空, 不能导出");
        }
        if (sheetMap.size() != dataMap.size() || sheetMap.size() != titleMap.size()) {
            throw new java.lang.RuntimeException("数据为空, 不能导出");
        }
        Integer totalColumns = dataMap.values().stream().map(x -> x.size()).reduce((x, y) -> x + y).get();
        Workbook workbook = totalColumns > ROWS_CHANGE ? new SXSSFWorkbook() : new XSSFWorkbook();
        Sheet sheet;
        Row row;
        Iterator<String> iterator = dataMap.keySet().iterator();
        long count = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            sheet = workbook.createSheet(StringUtils.isBlank(sheetMap.get(key)) ? key : sheetMap.get(key));
            Cell cell;
            row = sheet.createRow(0);
            Map<String, String> singleTitleMap = titleMap.get(key).get(0);
            List<String> titleEnList = singleTitleMap.keySet().stream().collect(Collectors.toList());
            for (int i = 0; i < titleEnList.size(); i++) {
                String titleEn = titleEnList.get(i);
                String titleCn = singleTitleMap.get(titleEn);
                cell = row.createCell(i);
                cell.setCellValue(StringUtils.isNotEmpty(titleCn) ? titleCn : titleEn);
            }

            List<T> dataList = dataMap.get(key);
            count += dataList.size();
            for (int i = 0; i < dataList.size(); i++) {
                row = sheet.createRow(i + 1);
                Map<String, Object> map = dataList.get(i);
                int index = 0;
                for (String title : titleEnList) {
                    Object obj = map.get(title);
                    if (Objects.nonNull(obj)) {
                        row.createCell(index).setCellValue(obj.toString());
                    } else {
                        row.createCell(index);
                    }
                    index++;
                }
            }
        }
        stopWatch.stop();
        logger.info("生成Excel文件结束，执行时间为[{}]s", stopWatch.getTime(TimeUnit.SECONDS));
        logger.info("生成Excel文件结束，行数为[{}]", count);
        return workbook;
    }

    /**
     * 创建保存
     *
     * @param sheetMap
     * @param dataMap
     * @param titleMap
     * @param excelPath
     * @param <T>
     */
    public static <T extends Map<String, Object>> void createExcel(Map<String, String> sheetMap, Map<String, List<T>> dataMap, Map<String, List<Map<String, String>>> titleMap, String excelPath) {
        Workbook workbook = createExcel(sheetMap, dataMap, titleMap);
        File file = new File(excelPath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建并下载
     *
     * @param sheetMap
     * @param dataMap
     * @param titleMap
     * @param fileName
     * @param response
     * @param <T>
     */
    public static <T extends Map<String, Object>> void createExcel(Map<String, String> sheetMap, Map<String, List<T>> dataMap, Map<String, List<Map<String, String>>> titleMap, String fileName, HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.addHeader("Param", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        String name;
        try {
            name = new String(fileName.getBytes(Charset.forName("UTF-8")), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e.getMessage());
        }
        name += SUFFIX;
        response.addHeader("Content-Disposition", "attachment;filename=" + name);
        try (OutputStream outputStream = response.getOutputStream();
             Workbook workbook = createExcel(sheetMap, dataMap, titleMap)) {
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e.getMessage());
        }
    }

    /**
     * 导出excl
     *
     * @param title
     * @param dataMap
     * @param type
     * @param response
     * @param <T>
     */
    public static <T extends Map<String, Object>> void createExportExcel(List<String> title, List<Map<String, Object>> dataMap, String type, HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.addHeader("Param", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        String name;
        try {
            name = new String(ExcelKey.getCn(type).getBytes(Charset.forName("UTF-8")), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e.getMessage());
        }
        name += SUFFIX;
        response.addHeader("Content-Disposition", "attachment;filename=" + name);
        try (OutputStream outputStream = response.getOutputStream();
             Workbook workbook = exportExcl(dataMap, title, type)) {
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e.getMessage());
        }
    }

    /**
     * 下载
     *
     * @param fileName
     * @param response
     */
    public static void download(String fileName, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            File file = new File(fileName);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.addHeader("Param", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            String name = new String(file.getName().getBytes(Charset.forName("UTF-8")), "ISO8859-1");
            response.addHeader("Content-Disposition", "attachment;filename=" + name);
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
            throw new java.lang.RuntimeException(e.getMessage());
        }
    }

    public static <T extends Map<String, Object>> Workbook exportExcl(List<Map<String, Object>> result, List<String> title, String type) {
        logger.info("开始生成Excel文件，数据总量为[{}]", result.size());
        StopWatch stopWatch = StopWatch.createStarted();

        if (org.springframework.util.CollectionUtils.isEmpty(result)) {
            throw new java.lang.RuntimeException("数据为空, 不能导出");
        }
        Integer totalColumns = result.size();
        Workbook workbook = totalColumns > ROWS_CHANGE ? new SXSSFWorkbook() : new XSSFWorkbook();
        Sheet sheet;
        Row row;
        sheet = workbook.createSheet(ExcelKey.getCn(type));
        Cell cell;
        row = sheet.createRow(0);
        for (int i = 0; i < title.size(); i++) {
            String titleEn = title.get(i);
            String titleCn = ExcelKey.getCn(titleEn);
            cell = row.createCell(i);
            cell.setCellValue(StringUtils.isNotEmpty(titleCn) ? titleCn : titleEn);
        }
        for (int i = 0; i < result.size(); i++) {
            //根据表头取内容
            row = sheet.createRow(i + 1);
            int index = 0;
            for (String t : title) {
                Object obj = result.get(i).get(t);
                if (Objects.nonNull(obj)) {
                    row.createCell(index).setCellValue(obj.toString());
                } else {
                    row.createCell(index);
                }
                index++;
            }
        }
        stopWatch.stop();
        logger.info("生成Excel文件结束，执行时间为[{}]s", stopWatch.getTime(TimeUnit.SECONDS));
        logger.info("生成Excel文件结束，行数为[{}]", result.size());
        return workbook;
    }

    @Getter
    public enum ExcelKey {
        /**
         * 情报研判
         */
        QBYP("0", "情报研判"),
        ;

        private final String name;

        private final String key;

        ExcelKey(String name, String key) {
            this.name = name;
            this.key = key;
        }

        public static String getCn(String name) {
            for (ExcelKey examType : ExcelKey.values()) {
                if (examType.getName().equals(name)) {
                    return examType.getKey();
                }
            }
            return null;
        }
    }
}
