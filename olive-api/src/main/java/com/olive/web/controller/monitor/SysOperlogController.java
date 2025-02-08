package com.olive.web.controller.monitor;

import com.olive.framework.enums.BusinessType;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.framework.web.system.SysOperLog;
import com.olive.framework.web.system.dto.SysOperLogSearch;
import com.olive.system.service.SysOperLogService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/monitor/operlog")
public class SysOperlogController {
    private final SysOperLogService sysOperLogService;

    @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
    @GetMapping("/list")
    public Page<SysOperLog> list(SysOperLogSearch search, PageQuery page) {
        return sysOperLogService.page(search, page);
    }

    // @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('monitor:operlog:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysOperLog operLog) {
    //     List<SysOperLog> list = operLogService.selectOperLogList(operLog);
    //     ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
    //     util.exportExcel(response, list, "操作日志");
    // }


    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/{operIds}")
    public int remove(@PathVariable(name = "operIds") Long[] operIds) {
        return sysOperLogService.deleteOperLogByIds(List.of(operIds));
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public void clean() {
        sysOperLogService.cleanOperLog();
    }
}
