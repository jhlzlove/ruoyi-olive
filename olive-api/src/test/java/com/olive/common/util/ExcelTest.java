package com.olive.common.util;

import com.olive.framework.web.system.SysLoginLog;
import com.olive.framework.web.system.SysLoginLogDraft;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author jhlz
 * @version x.x.x
 */
public class ExcelTest {
    /**
     * example:
     **/
    @Test
    public void excel_export_Test() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("登录日志");
        // 标题行
        Row head = sheet.createRow(0);
        head.createCell(0).setCellValue("编号");
        head.createCell(1).setCellValue("名称");
        head.createCell(2).setCellValue("地址");
        head.createCell(3).setCellValue("地点");
        head.createCell(4).setCellValue("操作系统");
        head.createCell(5).setCellValue("浏览器");
        head.createCell(6).setCellValue("访问时间");

        SysLoginLog log = new SysLoginLogDraft.Builder()
                .infoId(2L)
                .os("Linux")
                .browser("Firefox")
                .loginTime(LocalDateTime.now())
                .loginLocation("China")
                .userName("诗酒趁年华")
                .build();

        Map<String, ImmutableProp> immutableProp = ImmutableType.get(SysLoginLog.class).getProps();
        XSSFRow row = sheet.createRow(1);

        immutableProp.values().forEach(v -> {
            ImmutableSpi e = (ImmutableSpi) log;
            if (e.__isLoaded(v.getId())) {
                int index = switch (v.getName()) {
                    case "infoId" -> 0;
                    case "userName" -> 1;
                    case "ipaddr" -> 2;
                    case "loginLocation" -> 3;
                    case "browser" -> 4;
                    case "os" -> 5;
                    case "loginTime" -> 6;
                    default -> 10;
                };
                System.out.println(v.getId() + " " + v.getName() + " : " + e.__get(v.getId()));
                row.createCell(index).setCellValue(e.__get(v.getId()).toString());
            }
        });

        try (FileOutputStream fileOutputStream = new FileOutputStream("111.xlsx")) {
            workbook.write(fileOutputStream);
            fileOutputStream.flush();
            workbook.close();
        } catch (FileNotFoundException exp) {
            throw new RuntimeException(exp);
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }
}
