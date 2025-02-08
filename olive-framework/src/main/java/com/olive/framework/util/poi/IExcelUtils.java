package com.olive.framework.util.poi;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName: IExcelUtils
 * @Description: 读取或导出excel工具类
 * @Date: 2023/6/1 9:57
 * @author: ph9527
 * @version: 1.0
 */
public class IExcelUtils {

    /**
     * 获取获取大标题行数时循环的行数
     */
    private final static Integer FIND_TITLE_SIZE = 10;
    /**
     * IExcel与当前exceldate类型共有的日期格式
     */
    public final static String IEXCEL_DATE_DEFAULT_FORMAT = "yyyy/MM/dd";

    /**
     * 处理布尔值的转换，注意：请将表示true的标识写在分割符"|"的左边，表示false的标识写在分割符“|”的右边 ；默认"是|否"
     */
    public final static String IEXCEL_BOOLEAN_TRUE_DEFAULT_FLAG = "\\|";
    /**
     * 默认标题合并列
     */
    public final static int Excel_DEFAULT_TITLE_MERGERNUM = 6;


    /**
     * 因缺失必填字段为读取的数据信息
     */
    private static List<BlankRowInfo> errRowInfo;

    /**
     * excel映射顶级对象class
     */
    private static Class clazz;


    static class ClassVoIExcelEntity {
        /**
         * 当前所属对象class
         */
        private Class clazz;

        private Class parentClass;

        //当前等级
        private Integer levelIndex;
        /**
         * 所有标注IExcel注解的各属性值
         */
        private List<ClassVoFieldIExcel> fields;
        /**
         * 标注IExcel的子集
         */
        private ClassVoIExcelEntity sonClassVo;
    }


    static class ClassVoFieldIExcel {
        private String classVoFieldName;

        private IExcel iExcel;
    }

    private static class CellTitleValueEntity {
        private String cellTitleName;
        private Integer cellIndex;
        //在当前行字段对应对象的第一个元素
        private boolean isExcelObjFirstField;
        //在当前行字段对应对象的最后一个元素
        private boolean isExcelObjLastField;
    }

    private static class MergedRowInfo {
        private Boolean isMerge;
        private Integer firstRow;
        private Integer lastRow;
    }

    public static class BlankRowInfo {
        //缺失信息的行号
        public Long rowNo;

        //当前对象级别
        public Integer objLevel;
        //缺失信息的列名
        public List<String> blankTitleNames;
    }


    private static class ExportColumn {
        private String excelColumnName;
        private String value;
    }

    private static class ExportRow {
        private List<ExportColumn> values;
        private List<ExportRow> sons;
        private Integer levelIndex;
        private Integer mergeNum;
    }

    private static class TitleEntity {
        private String name;
        private Integer sortNo;
    }


    /**
     * 读取excel文件
     *
     * @param inputStream 前端上传到后端的文件用的是MultipartFile接收，免得转File,就用inputStream做参数
     * @param fileName    文件名，必填，需要根据文件名判断是否是excel文件以及判断是xlsx或者xls，直接用file.getName()就行了
     * @param sourceClass
     * @param <T>
     * @return
     */
    public static <T> List<T> readExcel(InputStream inputStream, String fileName, Class<T> sourceClass) {
        //初始化
        clazz = null;
        List<T> data = new ArrayList<>();
        Workbook wb = null;
        try {
            //检测是否是excel文件
            if (!isExcelFile(fileName)) {
                throw new RuntimeException("当前文件不是excel文件");
            }
            //检测是否是xlsx,如果文件名为空，则默认xlsx
            wb = isXlsx(fileName) ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
        } catch (Exception e) {
            data = new ArrayList<>();
            throw new RuntimeException("excel文件解析错误：" + e);
        }
        if (wb != null) {
            //给顶级对象赋值
            clazz = sourceClass;
            errRowInfo = new ArrayList<>();
            Sheet sheet0 = wb.getSheetAt(0);
            int lastRowNum = sheet0.getLastRowNum();
            //总行数小于等于标题行则代表没有数据，直接返回；
            int rowCount = lastRowNum + 1;
            //获取映射对象字段值
            List<String> iExcelNames = new ArrayList<>();
            ClassVoIExcelEntity classVoIExcelEntity = new ClassVoIExcelEntity();
            classVoIExcelEntity.clazz = clazz;
            classVoIExcelEntity.levelIndex = 0;
            setAllClassVoIExcelEntity(classVoIExcelEntity, iExcelNames);
            int titleCount = getTitleCount(iExcelNames, sheet0);
            if (rowCount <= titleCount) {
                return data;
            }

            //读取数据
            try {
                readData(sheet0, titleCount, classVoIExcelEntity, data);
            } catch (Exception e) {
                throw new RuntimeException("读取excel数据错误:" + e);
            }
        }

        return data;

    }


    /**
     * 导出excel
     *
     * @param fileName
     * @param titleName
     * @param response
     * @param data
     * @param sourceClass
     */
    public static void exportExcel(String fileName, String titleName, HttpServletResponse response, List data, Class sourceClass) throws FileNotFoundException {
        Workbook workbook = null;
        OutputStream outputStream = null;
        try {
            workbook = new XSSFWorkbook();
            setExcelSheet(workbook, titleName, data, sourceClass);
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/octet-stream;charset=utf-8");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "utf-8"));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finallyContent(outputStream, workbook);
        }

    }

    /**
     * 异常
     *
     * @param
     * @return
     */
    public static void finallyContent(OutputStream outputStream, Workbook workbook) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 导出excel
     *
     * @param workbook
     * @param titleName
     * @param data
     * @param sourceClass
     */
    public static void setExcelSheet(Workbook workbook, String titleName, List data, Class sourceClass) {
        int rowIndex = 0;
        // 创建一个工作表
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet();
        clazz = sourceClass;
        //获取映射对象字段值

        ClassVoIExcelEntity classVoIExcelEntity = new ClassVoIExcelEntity();
        classVoIExcelEntity.clazz = clazz;
        classVoIExcelEntity.levelIndex = 0;
        setAllClassVoIExcelEntity(classVoIExcelEntity, new ArrayList<>());
        //获取排序好的所有列名
        List<String> iExcelNames = new ArrayList<>();
        getIexcelNames(classVoIExcelEntity, iExcelNames);

        //获取最后的层级，从0开始的
        Integer lastIndex = getLastLevel(classVoIExcelEntity);

        sheet.setDefaultColumnWidth(15);

        //设置大标题
        if (strIsNotBlank(titleName)) {
            createBigTitle(sheet, iExcelNames, titleName, rowIndex);
            rowIndex++;
        }

        if (listIsNotEmpty(iExcelNames)) {
            //设置小标题
            createSmallTitles(sheet, iExcelNames, rowIndex);
            rowIndex++;
            if (listIsNotEmpty(data)) {
                List<ExportRow> exportRows = new ArrayList<>();
                //设置数据与ExportRow的对应关系
                setExportDataMapping(classVoIExcelEntity, data, exportRows);
                //设置数据合并行数
                setMergeRowForExportRow(exportRows, lastIndex);
                //将数据输入到excel表格中
                setDataToExcel(exportRows, sheet, iExcelNames, rowIndex);
            }
        }

    }

    private static void setDataToExcel(List<ExportRow> exportRows, XSSFSheet sheet, List<String> iExcelNames, int startRowIndex) {
        for (ExportRow exportRow : exportRows) {
            Integer mergeNum = exportRow.mergeNum;
            List<ExportColumn> values = exportRow.values;
            XSSFRow row = nextRow(sheet, startRowIndex);
            int cellIndex = 0;
            boolean mergeFlag = !(mergeNum == 0 || mergeNum == 1);
            for (; cellIndex < values.size(); cellIndex++) {
                XSSFCell cell = row.createCell(cellIndex);
                cell.setCellValue(getValueFromExportColumn(iExcelNames.get(cellIndex), values));
                if (mergeFlag) {
                    int endIndex = startRowIndex + mergeNum - 1;
                    sheet.addMergedRegion(new CellRangeAddress(startRowIndex, endIndex, cellIndex, cellIndex));
                }
            }

            List<ExportRow> sons = exportRow.sons;
            if (listIsNotEmpty(sons)) {
                setSonDataToExcel(sons, sheet, iExcelNames, startRowIndex, cellIndex);
            }
            if (mergeFlag) {
                startRowIndex += mergeNum;
            } else {
                startRowIndex += 1;
            }
        }
    }

    /**
     * 设置子集的值到excel中
     *
     * @param sons
     * @param sheet
     * @param startRowIndex
     * @param startCellIndex
     */
    private static void setSonDataToExcel(List<ExportRow> sons, XSSFSheet sheet, List<String> iExcelNames, int startRowIndex, int startCellIndex) {
        Integer rowIndex = startRowIndex;

        for (ExportRow exportRow : sons) {
            Integer cellIndex = startCellIndex;
            Integer mergeNum = exportRow.mergeNum;
            List<ExportColumn> values = exportRow.values;
            XSSFRow row = null;
            if (sheet.getRow(rowIndex) == null) {
                row = nextRow(sheet, rowIndex);
            } else {
                row = sheet.getRow(rowIndex);
            }

            Integer endCellIndex = values.size() + startCellIndex;

            //合并标识
            boolean mergeFlag = !(mergeNum == 0 || mergeNum == 1);

            for (; cellIndex < endCellIndex; cellIndex++) {
                XSSFCell cell = row.createCell(cellIndex);
                cell.setCellValue(getValueFromExportColumn(iExcelNames.get(cellIndex), values));
                if (mergeFlag) {
                    int endIndex = rowIndex + mergeNum - 1;
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, endIndex, cellIndex, cellIndex));
                }
            }

            List<ExportRow> sons1 = exportRow.sons;

            if (mergeFlag) {
                rowIndex += mergeNum;
            } else {
                rowIndex += 1;
            }
            if (listIsNotEmpty(sons1)) {
                setSonDataToExcel(sons1, sheet, iExcelNames, row.getRowNum(), startCellIndex + values.size());
            }
        }


    }


    /**
     * 根据列名获取值
     *
     * @param name
     * @param values
     * @return
     */
    private static String getValueFromExportColumn(String name, List<ExportColumn> values) {
        for (ExportColumn exportColumn : values) {
            if (name.equals(exportColumn.excelColumnName)) {
                return exportColumn.value;
            }
        }
        return null;
    }


    /**
     * 获取最后的层级数
     *
     * @param classVoIExcelEntity
     * @return
     */
    private static Integer getLastLevel(ClassVoIExcelEntity classVoIExcelEntity) {
        ClassVoIExcelEntity sonClassVo = classVoIExcelEntity.sonClassVo;
        if (sonClassVo == null) {
            return classVoIExcelEntity.levelIndex;
        } else {
            return getLastLevel(sonClassVo);
        }
    }

    /**
     * 设置合并行数
     *
     * @param exportRows
     * @param lastIndex
     */
    private static void setMergeRowForExportRow(List<ExportRow> exportRows, Integer lastIndex) {
        for (ExportRow exportRow : exportRows) {
            List<ExportRow> sons = exportRow.sons;
            if (lastIndex == 0) {
                exportRow.mergeNum = 1;
            } else {
                exportRow.mergeNum = getMergeNum(sons, lastIndex);
                if (listIsNotEmpty(sons)) {
                    setMergeRowForExportRow(sons, lastIndex);
                }
            }


        }
    }

    private static int getMergeNum(List<ExportRow> sons, Integer lastIndex) {
        if (listIsNotEmpty(sons)) {
            ExportRow exportRow = sons.get(0);
            if (exportRow.levelIndex == lastIndex - 1) {
                AtomicInteger count = new AtomicInteger();
                sons.forEach(item -> {
                    List<ExportRow> sons1 = item.sons;
                    if (listIsNotEmpty(sons1)) {
                        count.addAndGet(sons1.size());
                    }
                });
                return count.get();
            } else if (exportRow.levelIndex == lastIndex) {
                return sons.size();
            } else {
                AtomicInteger flag = new AtomicInteger();
                sons.forEach(item -> {
                    flag.addAndGet(getMergeNum(item.sons, lastIndex));
                });
                return flag.get();
            }
        }
        return 0;
    }

    /**
     * 获取所有excel列名，并按顺序排列
     *
     * @param classVoIExcelEntity
     * @return
     */
    private static void getIexcelNames(ClassVoIExcelEntity classVoIExcelEntity, List<String> names) {
        if (classVoIExcelEntity != null) {
            List<ClassVoFieldIExcel> fields = classVoIExcelEntity.fields;
            ClassVoIExcelEntity sonClassVo = classVoIExcelEntity.sonClassVo;
            if (listIsNotEmpty(fields)) {
                List<IExcel> excelsSorted = new ArrayList<>();
                List<IExcel> excels = new ArrayList<>();
                for (int i = 0; i < fields.size(); i++) {
                    ClassVoFieldIExcel classVoFieldIExcel = fields.get(i);
                    IExcel iExcel = classVoFieldIExcel.iExcel;
                    if (iExcel != null && !iExcel.isSon()) {
                        if (iExcel.sortNo() == 0) {
                            excels.add(iExcel);
                        } else {
                            excelsSorted.add(iExcel);
                        }
                    }
                }
                //合并
                List<String> newNames = Stream.concat(
                        excelsSorted.stream().sorted(Comparator.comparingInt(IExcel::sortNo)).map(item -> item.name()),
                        excels.stream().map(item -> item.name())
                ).collect(Collectors.toList());

                names.addAll(newNames);
            }

            //是否是有集
            if (sonClassVo != null) {
                getIexcelNames(classVoIExcelEntity.sonClassVo, names);
            }
        }


    }

    /**
     * 设置数据与excel每列的对应关系
     *
     * @param classVoIExcelEntity
     * @param data
     * @param exportRows
     */
    private static void setExportDataMapping(ClassVoIExcelEntity classVoIExcelEntity, List data, List<ExportRow> exportRows) {
        for (int i = 0; i < data.size(); i++) {
            ExportRow exportRow = new ExportRow();
            List<ExportRow> sonExportRow = null;
            Object obj = data.get(i);
            Class<?> objClass = obj.getClass();
            ClassVoIExcelEntity classEntity = getClassEntityByClass(classVoIExcelEntity, objClass);
            if (classEntity != null) {
                List<ClassVoFieldIExcel> fieldIExcels = classEntity.fields;
                List<ExportColumn> columnList = new ArrayList<>();
                fieldIExcels.forEach(fieldIExcel -> {
                    try {
                        if (!fieldIExcel.iExcel.isSon()) {
                            Field field = objClass.getDeclaredField(fieldIExcel.classVoFieldName);
                            if (field != null) {
                                field.setAccessible(true);
                                ExportColumn exportColumn = new ExportColumn();
                                exportColumn.value = getFieldValue(obj, field, fieldIExcel.iExcel);
                                exportColumn.excelColumnName = fieldIExcel.iExcel.name();
                                columnList.add(exportColumn);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                exportRow.values = columnList;
                exportRow.levelIndex = classEntity.levelIndex;
                ClassVoIExcelEntity sonClassVo = classEntity.sonClassVo;
                if (sonClassVo != null) {
                    sonExportRow = new ArrayList<>();
                    List sonData = getSonData(obj);
                    if (listIsNotEmpty(sonData)) {
                        setExportDataMapping(classVoIExcelEntity, sonData, sonExportRow);
                        exportRow.sons = sonExportRow;
                    }
                }
                exportRows.add(exportRow);
            }

        }
    }

    /**
     * 获取对象的属性值
     *
     * @param obj
     * @param <T>
     * @return
     */
    private static <T> String getFieldValue(Object obj, Field field, IExcel annotation) throws Exception {
        String value = null;
        field.setAccessible(true);
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            if (annotation != null) {
                String s = annotation.booleanFormatter();
                String[] split = s.split(IEXCEL_BOOLEAN_TRUE_DEFAULT_FLAG);
                Object o = field.get(obj);
                if (o != null) {
                    Boolean aBoolean = (Boolean) field.get(obj);
                    value = aBoolean ? split[0] : split[1];
                } else {
                    value = "";
                }


            }
        } else if (field.getType() == Date.class) {
            if (annotation != null) {
                String dateForMatter = annotation.dateFormat();
                Object o = field.get(obj);
                if (o != null) {
                    Date date = (Date) field.get(obj);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateForMatter);
                    value = simpleDateFormat.format(date);
                } else {
                    value = "";
                }

            }
        } else {
            Object o = field.get(obj);
            value = o != null ? o.toString() : "";

        }

        return value;
    }

    /**
     * 获取子集
     *
     * @param obj
     * @return
     */
    private static List getSonData(Object obj) {
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            Class<?> type = field.getType();
            if (type == List.class) {
                IExcel annotation = field.getAnnotation(IExcel.class);
                if (annotation != null && annotation.isSon()) {
                    try {
                        field.setAccessible(true);
                        return (List) field.get(obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }

        return null;
    }

    /**
     * 根据class获取对应的classVoIExcelEntity
     *
     * @param classVoIExcelEntity
     * @param aClass
     * @return
     */
    private static ClassVoIExcelEntity getClassEntityByClass(ClassVoIExcelEntity classVoIExcelEntity, Class<?> aClass) {
        if (classVoIExcelEntity == null) return null;
        Class clazz1 = classVoIExcelEntity.clazz;
        if (clazz1.getTypeName().equals(aClass.getTypeName())) return classVoIExcelEntity;
        ClassVoIExcelEntity sonClassVo = classVoIExcelEntity.sonClassVo;
        return getClassEntityByClass(sonClassVo, aClass);
    }


    /**
     * 创建大标题样式
     *
     * @param wb
     * @return
     */
    public static XSSFCellStyle createBigTitleCellStyle(XSSFWorkbook wb) {
        // 标题样式（加粗，垂直居中）
        XSSFCellStyle titleCellStyle = wb.createCellStyle();
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        XSSFFont fontStyle = wb.createFont();
        fontStyle.setBold(true);   //加粗
        fontStyle.setFontHeightInPoints((short) 16);  //设置标题字体大小
        titleCellStyle.setFont(fontStyle);
        return titleCellStyle;
    }


    /**
     * 创建小标题样式
     *
     * @param wb
     * @return
     */
    public static XSSFCellStyle createSmallTitleCellStyle(XSSFWorkbook wb) {
        //设置表头样式，表头居中
        XSSFCellStyle style = wb.createCellStyle();
        //设置单元格样式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        //设置字体
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    /**
     * 设置大标题
     *
     * @param sheet
     * @param iExcelNames
     * @param titleName
     * @param rowIndex
     */
    private static void createBigTitle(XSSFSheet sheet, List<String> iExcelNames, String titleName, int rowIndex) {
        XSSFCellStyle style = createBigTitleCellStyle(sheet.getWorkbook());
        XSSFRow rowBigTitle = nextRow(sheet, rowIndex);
        //大标题
        XSSFCell cell = rowBigTitle.createCell(0);
        cell.setCellValue(titleName);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, listIsNotEmpty(iExcelNames) ? iExcelNames.size() - 1 : Excel_DEFAULT_TITLE_MERGERNUM));
    }

    /**
     * 设置小标题
     *
     * @param sheet
     * @param iExcelNames
     * @param rowIndex
     */
    private static void createSmallTitles(XSSFSheet sheet, List<String> iExcelNames, int rowIndex) {
        XSSFCellStyle style = createSmallTitleCellStyle(sheet.getWorkbook());
        XSSFRow rowSmallTitle = nextRow(sheet, rowIndex);
        for (int i = 0; i < iExcelNames.size(); i++) {
            XSSFCell cell = rowSmallTitle.createCell(i);
            cell.setCellValue(iExcelNames.get(i));
            cell.setCellStyle(style);
        }

    }

    /**
     * 获取下一行
     *
     * @param sheet
     * @param rowIndex
     * @return
     */
    private static XSSFRow nextRow(XSSFSheet sheet, int rowIndex) {
        XSSFRow row = sheet.createRow(rowIndex);
        return row;
    }

    /**
     * 读取数据
     *
     * @param sheet
     * @param titleCount
     * @param classVoIExcelEntity
     * @param data
     * @param <T>
     */
    private static <T> void readData(Sheet sheet, Integer titleCount, ClassVoIExcelEntity classVoIExcelEntity, List<T> data) throws Exception {
        //定义一个数组，存放各个层级的当前对象
        List levelsObjs = new ArrayList();
        //获取excel的所有小标题
        List<CellTitleValueEntity> cellTitles = getCellTitles(sheet, titleCount);
        if (listIsNotEmpty(cellTitles)) {
            //设置哪些是当前行的第一个元素
            setFirstAndLastField(cellTitles, classVoIExcelEntity);
            //开始读取数据
            int startDataIndex = titleCount + 1;
            int allRowNums = sheet.getLastRowNum() + 1;
            try {
                //循环excel每一行
                for (int i = startDataIndex; i < allRowNums; i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && checkRowDataIsNotBlank(cellTitles, row)) {
                        for (int j = 0; j < cellTitles.size(); j++) {
                            CellTitleValueEntity cellTitleValueEntity = cellTitles.get(j);
                            Integer nowCellIndex = cellTitleValueEntity.cellIndex;
                            Cell cell = row.getCell(nowCellIndex);
                            if (cell == null) {
                                cell = row.createCell(nowCellIndex);
                            }
                            String cellValue = getCellValue(cell);
                            //获取当前字段的所属class
                            ClassVoIExcelEntity classVoIExcelEntityNow = getClassVoExcelEntityByExcelCellTitle(cellTitleValueEntity.cellTitleName, classVoIExcelEntity);
                            Integer levelIndex = classVoIExcelEntityNow.levelIndex;
                            if (classVoIExcelEntityNow != null) {
                                //当前单元格是否为第一个字段
                                boolean isExcelObjFirstField = cellTitleValueEntity.isExcelObjFirstField;
                                boolean isExcelObjLastFieldField = cellTitleValueEntity.isExcelObjLastField;
                                //获取合并信息
                                MergedRowInfo mergedRowInfo = getMergedRowInfo(cell, sheet);
                                Boolean isMerge = mergedRowInfo.isMerge;
                                Integer firstRow = mergedRowInfo.firstRow;
                                Integer lastRow = mergedRowInfo.lastRow;


                                //判断是否需要新建对象
                                if (isExcelObjFirstField && (!isMerge || i == firstRow)) {
                                    if (!listIsNotEmpty(levelsObjs) || levelsObjs.size() < levelIndex + 1) {
                                        levelsObjs.add(classVoIExcelEntityNow.clazz.newInstance());
                                    } else {
                                        levelsObjs.set(levelIndex, classVoIExcelEntityNow.clazz.newInstance());
                                    }
                                }
                                //获取当前对象
                                Object nowObj = levelsObjs.get(classVoIExcelEntityNow.levelIndex);

                                //获取当前单元格对应对象的字段名
                                String fieldName = getFieldNameByCellTitleName(cellTitleValueEntity.cellTitleName, classVoIExcelEntityNow);

                                //给属性设置值
                                if (strIsNotBlank(cellValue)) {
                                    setCellValueToObjField(nowObj, fieldName, cellValue);
                                }

                                //获取当前对象的上级对象
                                Object parentObj = getParentObj(classVoIExcelEntityNow, levelsObjs);
                                List sonList = getSonListByParentObj(parentObj);
                                //判断当前单元格是否是所属对象最后的一个对应，借此判断当前行当前对象是否填充完成
                                if (isExcelObjLastFieldField && (!isMerge || i == lastRow)) {
                                    //判断必填字段是否都有值
                                    BlankRowInfo blankRowInfo = checkObjIsNotBlankField(classVoIExcelEntityNow, nowObj, (long) (i + 1));
                                    if (blankRowInfo == null) {
                                        if (parentObj != null) {
                                            sonList.add(nowObj);
                                        } else {
                                            data.add((T) nowObj);
                                        }
                                    } else {
                                        errRowInfo.add(blankRowInfo);
                                        setSonFieldValueToNull(nowObj);
                                        break;
                                    }

                                }


                            }

                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取未被读取的错误信息
     *
     * @return
     */
    public static List<BlankRowInfo> getErrRowInfo() {
        return errRowInfo;
    }

    /**
     * 设置子集值为空
     *
     * @param nowObj
     */
    private static void setSonFieldValueToNull(Object nowObj) {
        if (nowObj != null) {
            Class<?> parentObjClass = nowObj.getClass();
            Field[] declaredFields = parentObjClass.getDeclaredFields();
            for (Field field : declaredFields) {
                IExcel annotation = field.getAnnotation(IExcel.class);
                if (annotation != null) {
                    if (annotation.isSon()) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        if (type == List.class) {
                            try {
                                field.set(nowObj, null);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 检查当前对象必填字段是否都有值
     *
     * @param classVoIExcelEntityNow
     * @param nowObj
     */
    private static BlankRowInfo checkObjIsNotBlankField(ClassVoIExcelEntity classVoIExcelEntityNow, Object nowObj, Long rowNo) {
        AtomicReference<BlankRowInfo> blankRowInfo = new AtomicReference<>();
        List<ClassVoFieldIExcel> fields = classVoIExcelEntityNow.fields;
        if (nowObj != null && listIsNotEmpty(fields)) {
            Class<?> clazz = nowObj.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            Arrays.stream(declaredFields).forEach(field -> {
                field.setAccessible(true);
                try {
                    Object fieldValueObj = field.get(nowObj);
                    IExcel iExcel = field.getAnnotation(IExcel.class);
                    if (fieldValueObj == null && iExcel != null && iExcel.isNotBlank()) {
                        if (blankRowInfo.get() == null) {
                            blankRowInfo.set(new BlankRowInfo());
                            blankRowInfo.get().rowNo = rowNo;
                            blankRowInfo.get().objLevel = classVoIExcelEntityNow.levelIndex;
                            blankRowInfo.get().blankTitleNames = new ArrayList<>();
                        }
                        blankRowInfo.get().blankTitleNames.add(iExcel.name());
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            });


        }
        return blankRowInfo.get();
    }


    /**
     * 获取上级对象的子集
     *
     * @param parentObj
     * @return
     */
    private static List getSonListByParentObj(Object parentObj) {
        if (parentObj != null) {
            Class<?> parentObjClass = parentObj.getClass();
            Field[] declaredFields = parentObjClass.getDeclaredFields();
            for (Field field : declaredFields) {
                IExcel annotation = field.getAnnotation(IExcel.class);
                if (annotation != null) {
                    if (annotation.isSon()) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        if (type == List.class) {
                            try {
                                Object son = field.get(parentObj);
                                if (son != null) {
                                    return (List) son;
                                }
                                List sonList = new ArrayList<>();
                                field.set(parentObj, sonList);
                                return sonList;
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            }
        }
        return null;


    }

    /**
     * 获取父级对象
     *
     * @param classVoIExcelEntityNow
     * @param levelsObjs
     * @return
     */
    private static Object getParentObj(ClassVoIExcelEntity classVoIExcelEntityNow, List levelsObjs) {
        Object parentObj = null;
        Class parentClass = classVoIExcelEntityNow.parentClass;
        if (parentClass != null) {
            String typeName = parentClass.getTypeName();
            for (Object item : levelsObjs) {
                if (typeName.equals(item.getClass().getTypeName())) {
                    parentObj = item;
                    break;
                }
            }
        }


        return parentObj;
    }

    /**
     * 获取当前单元格对应对象的字段名
     *
     * @param cellTitleName
     * @param classVoIExcelEntityNow
     * @return
     */
    private static String getFieldNameByCellTitleName(String cellTitleName, ClassVoIExcelEntity classVoIExcelEntityNow) {
        List<ClassVoFieldIExcel> fields = classVoIExcelEntityNow.fields;
        for (ClassVoFieldIExcel classVoFieldIExcel : fields) {
            if (classVoFieldIExcel.iExcel.name().equals(cellTitleName)) {
                return classVoFieldIExcel.classVoFieldName;
            }
        }
        return null;
    }

    /**
     * 给属性设置值
     *
     * @param obj
     * @param filedName
     * @param cellValue
     * @param <T>
     */
    private static <T> void setCellValueToObjField(Object obj, String filedName, Object cellValue) throws Exception {
        Class<?> clazz = obj.getClass();
        Field declaredField = clazz.getDeclaredField(filedName);
        declaredField.setAccessible(true);
        Class<?> type = declaredField.getType();
        if (type == Integer.class || type == int.class) {
            declaredField.set(obj, Integer.parseInt(cellValue.toString()));
        } else if (type == Long.class || type == long.class) {
            declaredField.set(obj, Long.parseLong(cellValue.toString()));
        } else if (type == boolean.class || type == Boolean.class) {
            IExcel annotation = declaredField.getAnnotation(IExcel.class);
            String s = annotation.booleanFormatter();
            String[] split = s.split(IEXCEL_BOOLEAN_TRUE_DEFAULT_FLAG);
            declaredField.set(obj, split[0].trim().equals(cellValue.toString().trim()) ? true : false);
        } else if (type == Date.class) {
            IExcel annotation = declaredField.getAnnotation(IExcel.class);
            String s = annotation.dateFormat();
            SimpleDateFormat format = new SimpleDateFormat(s);
            declaredField.set(obj, format.parse(cellValue.toString()));
        } else if (type == Double.class || type == double.class) {
            declaredField.set(obj, Double.valueOf(cellValue.toString()));
        } else if (type == List.class) {
            declaredField.set(obj, cellValue);
        } else if (type == BigDecimal.class) {
            BigDecimal bigDecimal = BigDecimal.valueOf(Long.valueOf(cellValue.toString()));
            declaredField.set(obj, bigDecimal);
        } else {
            declaredField.set(obj, cellValue);
        }
    }

    /**
     * 设置哪些是当前行的第一个元素和最后一个元素 （注意：这里要求不能出现重复的列名）
     *
     * @param cellTitles
     * @param classVoIExcelEntity
     */
    private static void setFirstAndLastField(List<CellTitleValueEntity> cellTitles, ClassVoIExcelEntity classVoIExcelEntity) {
        final ClassVoIExcelEntity[] previousClassVo = {null};
        for (int i = 0; i < cellTitles.size(); i++) {
            CellTitleValueEntity item = cellTitles.get(i);
            String cellTitleName = item.cellTitleName;
            ClassVoIExcelEntity nowClassVoEntity = getClassEntityByCellTitleName(cellTitleName, classVoIExcelEntity);
            if (nowClassVoEntity != null) {
                //设置是否是第一个元素
                if (previousClassVo[0] == null) {
                    item.isExcelObjFirstField = true;
                    previousClassVo[0] = nowClassVoEntity;
                } else if (!previousClassVo[0].clazz.getTypeName().equals(nowClassVoEntity.clazz.getTypeName())) {
                    item.isExcelObjFirstField = true;
                    previousClassVo[0] = nowClassVoEntity;

                }

                //设置是否是最后一个元素
                if (i < cellTitles.size() - 1) {
                    CellTitleValueEntity nextItem = cellTitles.get(i + 1);
                    String nextCellTitleName = nextItem.cellTitleName;
                    ClassVoIExcelEntity nextClassVoEntity = getClassEntityByCellTitleName(nextCellTitleName, classVoIExcelEntity);
                    if (nextClassVoEntity != null) {
                        if (!nowClassVoEntity.clazz.getTypeName().equals(nextClassVoEntity.clazz.getTypeName())) {
                            item.isExcelObjLastField = true;
                        }

                    }
                }
                if (i == cellTitles.size() - 1) {
                    item.isExcelObjLastField = true;
                }
            }

        }
    }


    /**
     * 根据列名获取 ClassVoIExcelEntity
     *
     * @param cellTitleName
     * @param classVoIExcelEntity
     * @return
     */
    private static ClassVoIExcelEntity getClassEntityByCellTitleName(String cellTitleName, ClassVoIExcelEntity classVoIExcelEntity) {
        List<ClassVoFieldIExcel> fields = classVoIExcelEntity.fields;
        for (ClassVoFieldIExcel item : fields) {
            IExcel iExcel = item.iExcel;
            String name = iExcel.name();
            if (name.equals(cellTitleName)) {
                return classVoIExcelEntity;
            }
        }

        ClassVoIExcelEntity sonClassVo = classVoIExcelEntity.sonClassVo;
        if (sonClassVo != null) {
            return getClassEntityByCellTitleName(cellTitleName, sonClassVo);
        }
        return null;

    }


    /**
     * 获取当前字段所属excel映射对象
     *
     * @param cellTitleName
     * @param classVoIExcelEntity
     * @return
     */
    private static ClassVoIExcelEntity getClassVoExcelEntityByExcelCellTitle(String cellTitleName, ClassVoIExcelEntity classVoIExcelEntity) {
        ClassVoIExcelEntity rsClassVoIExcelEntity = null;
        List<ClassVoFieldIExcel> fields = classVoIExcelEntity.fields;
        for (ClassVoFieldIExcel classVoFieldIExcel : fields) {
            IExcel iExcel = classVoFieldIExcel.iExcel;
            String name = iExcel.name();
            if (name.equals(cellTitleName)) {
                rsClassVoIExcelEntity = classVoIExcelEntity;
                break;
            }
        }

        if (rsClassVoIExcelEntity == null && classVoIExcelEntity.sonClassVo != null) {
            rsClassVoIExcelEntity = getClassVoExcelEntityByExcelCellTitle(cellTitleName, classVoIExcelEntity.sonClassVo);
        }

        return rsClassVoIExcelEntity;

    }

    /**
     * 获取标题总行数
     * iExcelNames
     *
     * @param sheet0
     * @return
     */
    private static int getTitleCount(List<String> iExcelNames, Sheet sheet0) {
        for (int i = 0; i < FIND_TITLE_SIZE; i++) {
            Row row = sheet0.getRow(i);
            if (row != null) {
                int lastCellNum = row.getLastCellNum();
                for (int j = 0; j < lastCellNum; j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = getCellValue(cell);
                    if (strIsNotBlank(cellValue) && iExcelNames.contains(cellValue)) {
                        return i;
                    }
                }
            }
        }

        return 0;
    }


    /**
     * 获取合并行信息
     *
     * @param cell
     * @param sheet
     * @return
     */
    private static MergedRowInfo getMergedRowInfo(Cell cell, Sheet sheet) {
        MergedRowInfo mergedRowInfo = new MergedRowInfo();
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                mergedRowInfo.isMerge = true;
                mergedRowInfo.firstRow = region.getFirstRow();
                mergedRowInfo.lastRow = region.getLastRow();
                return mergedRowInfo;
            }
        }
        mergedRowInfo.isMerge = false;
        return mergedRowInfo;
    }


    /**
     * 检查当前行不是空的
     *
     * @param cellTitles
     * @param row
     * @return
     */
    private static boolean checkRowDataIsNotBlank(List<CellTitleValueEntity> cellTitles, Row row) {
        AtomicInteger flag = new AtomicInteger();
        for (CellTitleValueEntity title : cellTitles) {
            Integer cellIndex = title.cellIndex;
            Cell cell = row.getCell(cellIndex);
            if (cell != null) {
                MergedRowInfo mergedRowInfo = getMergedRowInfo(cell, row.getSheet());
                if (mergedRowInfo != null && mergedRowInfo.isMerge) {
                    return true;
                }
                if (strIsNotBlank(getCellValue(cell))) {
                    flag.getAndIncrement();
                }
            }

        }
        return flag.get() > 0;
    }

    /**
     * 获取所有小标题
     *
     * @param sheet
     * @param titleCount
     * @return
     */
    private static List<CellTitleValueEntity> getCellTitles(Sheet sheet, Integer titleCount) {
        List<CellTitleValueEntity> cellTitles = new ArrayList<>();
        Row cellTitleRow = sheet.getRow(titleCount);
        if (cellTitleRow != null) {
            int lastCellNum = cellTitleRow.getLastCellNum();

            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = cellTitleRow.getCell(i);
                String cellValue = getCellValue(cell);
                if (strIsNotBlank(cellValue)) {
                    CellTitleValueEntity cellTitleEntity = new CellTitleValueEntity();
                    cellTitleEntity.cellTitleName = cellValue;
                    cellTitleEntity.cellIndex = i;
                    cellTitles.add(cellTitleEntity);
                }
            }
        }
        return cellTitles;
    }

    /**
     * 获取excel表格单元格的值
     *
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case NUMERIC:
                case FORMULA: {
                    // 判断当前的cell是否为Date
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat(IEXCEL_DATE_DEFAULT_FORMAT);
                        cellvalue = sdf.format(date);
                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = new DecimalFormat("0").format(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }


    /**
     * 设置所有iexcel注解与目标对象的映射字段名和值
     *
     * @param classVoIExcelEntity classVoIExcelEntity.clazz必须有值
     * @param iExcelNames
     */
    private static void setAllClassVoIExcelEntity(ClassVoIExcelEntity classVoIExcelEntity, List<String> iExcelNames) {
        AtomicBoolean isSonFlag = new AtomicBoolean(false);
        Class nowClazz = classVoIExcelEntity.clazz;
        List<ClassVoFieldIExcel> classVoFields = new ArrayList<>();
        Field[] declaredFields = nowClazz.getDeclaredFields();
        Arrays.stream(declaredFields).forEach(field -> {
            IExcel iExcel = field.getAnnotation(IExcel.class);
            if (iExcel != null && !iExcel.hiddenField()) {
                ClassVoFieldIExcel classVoFieldIExcel = new ClassVoFieldIExcel();
                if (iExcel.isSon() && !isSonFlag.get()) {
                    //处理子集
                    Class sonClass = getGenericClassForList(field);
                    if (sonClass != null) {
                        ClassVoIExcelEntity sonClassVoIExcel = new ClassVoIExcelEntity();
                        sonClassVoIExcel.clazz = sonClass;
                        sonClassVoIExcel.parentClass = classVoIExcelEntity.clazz;
                        sonClassVoIExcel.levelIndex = classVoIExcelEntity.levelIndex + 1;
                        setAllClassVoIExcelEntity(sonClassVoIExcel, iExcelNames);
                        classVoIExcelEntity.sonClassVo = sonClassVoIExcel;
                        isSonFlag.set(true);
                    }
                } else {
                    iExcelNames.add(iExcel.name());
                }
                classVoFieldIExcel.classVoFieldName = field.getName();
                classVoFieldIExcel.iExcel = iExcel;
                classVoFields.add(classVoFieldIExcel);
            }

        });
        classVoIExcelEntity.fields = classVoFields;
    }

    /**
     * 获取List的泛型类型
     *
     * @param field
     * @return
     */
    private static Class getGenericClassForList(Field field) {
        Class clazz = null;
        if (field.getType() == List.class) {
            Type genericType = field.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                clazz = (Class<?>) pt.getActualTypeArguments()[0];
            }
        }
        return clazz;
    }


    /**
     * 判断是否是excel文件
     *
     * @param fileName
     * @return
     */
    private static boolean isExcelFile(String fileName) {
        if (!strIsNotBlank(fileName)) {
            return false;
        }
        String fileNameSuffix = getFileNameSuffix(fileName);
        if (fileNameSuffix.toLowerCase().equals("xlsx") || fileNameSuffix.toLowerCase().equals("xls")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 检测excel类型,如果fileName为空，则默认为xlsx
     *
     * @param fileName
     * @return
     */
    private static boolean isXlsx(String fileName) {
        if (!strIsNotBlank(fileName)) {
            return true;
        }
        String fileNameSuffix = getFileNameSuffix(fileName);
        if (fileNameSuffix.toLowerCase().equals("xlsx")) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 获取文件后缀名
     *
     * @param fileName
     * @return
     */
    private static String getFileNameSuffix(String fileName) {
        String[] split = fileName.split("\\u002E");
        String fileNameSuffix = split[split.length - 1];
        return fileNameSuffix;
    }

    /**
     * 检测字符串不为空
     *
     * @param str
     * @return
     */
    private static boolean strIsNotBlank(String str) {
        return !(str == null || str.trim().equals(""));
    }

    /**
     * 判断list不为空
     *
     * @param list
     * @return
     */
    private static boolean listIsNotEmpty(List list) {
        return list != null && list.size() > 0;
    }

    /**
     * 隐藏字段
     *
     * @param clazz
     * @param columnName
     * @param target
     * @throws Exception
     */
    public static void hideColumn(Class<?> clazz, String columnName, Boolean target) throws Exception {
        if (!strIsNotBlank(columnName)) {
            throw new NullPointerException("传入的属性列名为空");
        }
        if (target == null) {
            target = true;
        }
        //获取目标对象的属性值
        Field field = clazz.getDeclaredField(columnName);
        //获取注解反射对象
        IExcel excelAnion = field.getAnnotation(IExcel.class);
        //获取代理
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(excelAnion);
        Field excelField = invocationHandler.getClass().getDeclaredField("memberValues");
        excelField.setAccessible(true);
        Map memberValues = (Map) excelField.get(invocationHandler);
        memberValues.put("hiddenField", target);
    }
}

