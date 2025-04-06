package com.olive.api.monitor;

import com.olive.framework.log.Log;
import com.olive.model.SysLoginLog;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysLoginLogSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.SysLoginLogService;
import com.olive.service.SysPasswordService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 系统访问记录
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/logininfor")
@AllArgsConstructor
public class SysLoginLogController {

    private static final Logger log = LoggerFactory.getLogger(SysLoginLogController.class);
    private final SysLoginLogService loginLogService;
    private final SysPasswordService passwordService;

    /**
     * 获取系统访问记录列表
     *
     * @param search
     * @return
     */
    @PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
    @GetMapping("/list")
    public Page<SysLoginLog> page(SysLoginLogSearch search, PageQuery page) {
        return loginLogService.page(search, page);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        Page<SysLoginLog> page = loginLogService.page(null, new PageQuery(1, 1000));
        List<SysLoginLog> rows = page.getRows();
        log.info("导出的记录总数：{}", rows.size());

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

        Collection<ImmutableProp> values = ImmutableType.get(SysLoginLog.class).getProps().values();

        for (int i = 0; i < rows.size(); i++) {
            // 创建新行
            Row row = sheet.createRow(i + 1);
            // 获取属性值
            SysLoginLog loginLog = rows.get(i);

            values.forEach(it -> {
                ImmutableSpi e = (ImmutableSpi) loginLog;
                PropId id = it.getId();
                if (e.__isLoaded(id)) {
                    int a = switch (it.getName()) {
                        case "infoId" -> 0;
                        case "userName" -> 1;
                        case "ipaddr" -> 2;
                        case "loginLocation" -> 3;
                        case "browser" -> 4;
                        case "os" -> 5;
                        case "loginTime" -> 6;
                        default -> 10;
                    };
                    // log.info("{} : {} : {}", id, it.getName(), e.__get(id));
                    row.createCell(a).setCellValue(e.__get(id).toString());
                }
            });
        }
        try (ServletOutputStream out = response.getOutputStream()) {
            // 03
            // response.setContentType("application/vnd.ms-excel;chartset=utf-8");
            // 07
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=users.xlsx");
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public boolean remove(@PathVariable(name = "ids") List<Long> ids) {
        return loginLogService.delete(ids);
    }

    /**
     * 清空登录日志
     */
    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public boolean clean() {
        return loginLogService.cleanLoginLog();
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:unlock')")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public void unlock(@PathVariable("userName") String userName) {
        passwordService.clearLoginRecordCache(userName);
    }
}
