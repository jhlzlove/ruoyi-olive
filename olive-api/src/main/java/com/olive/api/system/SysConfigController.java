package com.olive.api.system;

import com.olive.base.response.R;
import com.olive.framework.annotation.Anonymous;
import com.olive.framework.log.Log;
import com.olive.model.SysConfig;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysConfigSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.SysConfigService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/config")
public class SysConfigController {
    private final SysConfigService configService;

    /**
     * 获取参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/list")
    public Page<SysConfig> list(SysConfigSearch search, PageQuery page) {
        return configService.page(search, page);
    }

    // @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('system:config:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysConfig config) {
    //     List<SysConfig> list = configService.selectConfigList(config);
    //     ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
    //     util.exportExcel(response, list, "参数数据");
    // }

    /**
     * 根据参数编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @GetMapping(value = "/{configId}")
    public SysConfig getInfo(@PathVariable(name = "configId") long configId) {
        return configService.info(configId);
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey}")
    @Anonymous
    public R getConfigKey(@PathVariable(name = "configKey") String configKey) {
        return R.ok(configService.listByConfigKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysConfig config) {
        return configService.add(config);
    }

    /**
     * 修改参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysConfig config) {
        return configService.update(config);
    }

    /**
     * 删除参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public boolean remove(@PathVariable(name = "ids") List<Long> ids) {
        return configService.delete(ids);
    }

    /**
     * 刷新参数缓存
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public boolean refreshCache() {
        configService.resetConfigCache();
        return true;
    }
}
