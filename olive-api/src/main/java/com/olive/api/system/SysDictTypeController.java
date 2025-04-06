package com.olive.api.system;

import com.olive.framework.log.Log;
import com.olive.model.SysDictType;
import com.olive.model.constant.BusinessType;
import com.olive.model.record.PageQuery;
import com.olive.service.DictTypeService;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/dict/type")
@AllArgsConstructor
public class SysDictTypeController {
    private final DictTypeService dictTypeService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public Page<SysDictType> list(PageQuery page) {
        return dictTypeService.page(null, page);
    }

    // @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('system:dict:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysDictType dictType) {
    //     List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
    //     ExcelUtil<SysDictType> util = new ExcelUtil<SysDictType>(SysDictType.class);
    //     util.exportExcel(response, list, "字典类型");
    // }

    /**
     * 查询字典类型详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictId}")
    public SysDictType getInfo(@PathVariable(name = "dictId") long dictId) {
        return dictTypeService.info(dictId);
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysDictType dict) {
        return dictTypeService.add(dict);
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit(@Validated @RequestBody SysDictType dict) {
        return dictTypeService.update(dict);
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public void remove(@PathVariable(name = "ids") List<Long> ids) {
        dictTypeService.delete(ids);
    }

    /**
     * 刷新字典缓存
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public void refreshCache() {
        dictTypeService.resetDictCache();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/optionselect")
    public List<SysDictType> optionselect() {
        return  dictTypeService.list();
    }
}
