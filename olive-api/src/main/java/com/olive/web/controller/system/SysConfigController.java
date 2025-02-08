package com.olive.web.controller.system;

import com.olive.common.response.R;
import com.olive.framework.annotation.Anonymous;
import com.olive.framework.enums.BusinessType;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.system.domain.SysConfig;
import com.olive.system.domain.dto.SysConfigSearch;
import com.olive.system.service.SysConfigService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置 信息操作处理
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/config")
public class SysConfigController {
    private final SysConfigService configService2;

    /**
     * 获取参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/list")
    public Page<SysConfig> list(SysConfigSearch search, PageQuery page) {
        return configService2.page(search, page);
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
        return configService2.selectConfigById(configId);
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey}")
    @Anonymous
    public R getConfigKey(@PathVariable(name = "configKey") String configKey) {
        return R.ok(configService2.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysConfig config) {
        return configService2.add(config);
    }

    /**
     * 修改参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysConfig config) {
        return configService2.edit(config);
    }

    /**
     * 删除参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public boolean remove(@PathVariable(name = "configIds") Long[] configIds) {
        return configService2.deleteConfigByIds(configIds);
    }

    /**
     * 刷新参数缓存
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public boolean refreshCache() {
        configService2.resetConfigCache();
        return true;
    }
}
