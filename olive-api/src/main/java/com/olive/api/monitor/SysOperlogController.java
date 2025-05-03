package com.olive.api.monitor;

import com.olive.service.aop.log.Log;
import com.olive.model.SysOperLog;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysOperLogSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.SysOperLogService;
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
    @DeleteMapping("/{ids}")
    public boolean remove(@PathVariable(name = "ids") List<Long> ids) {
        return sysOperLogService.delete(ids);
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping("/clean")
    public void clean() {
        sysOperLogService.cleanOperLog();
    }
}
