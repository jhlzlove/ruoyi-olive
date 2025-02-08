package com.olive.web.controller.system;

import com.olive.framework.enums.BusinessType;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.system.domain.SysDictData;
import com.olive.system.domain.dto.SysDictDataSearch;
import com.olive.system.service.DictDataService;
import com.olive.system.service.DictTypeService;
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
@RequestMapping("/system/dict/data")
@AllArgsConstructor
public class SysDictDataController {
    private final DictDataService dictDataService;
    private final DictTypeService dictTypeService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public Page<SysDictData> list(SysDictDataSearch search, PageQuery pageRecord) {
        return dictDataService.selectDictDataList(search, pageRecord);
    }

    // @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('system:dict:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysDictData dictData) {
    //     List<SysDictData> list = dictDataService.selectDictDataList(dictData);
    //     ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
    //     util.exportExcel(response, list, "字典数据");
    // }

    /**
     * 查询字典数据详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictCode}")
    public SysDictData getInfo(@PathVariable(name = "dictCode") long dictCode) {
        return dictDataService.selectDictDataById(dictCode);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/type/{dictType}")
    public List<SysDictData> dictType(@PathVariable(name = "dictType") String dictType) {
        return  dictTypeService.selectDictDataByType(dictType);
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    public int add(@Validated @RequestBody SysDictData dict) {
        return dictDataService.insertDictData(dict);
    }

    /**
     * 修改保存字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit(@Validated @RequestBody SysDictData dict) {
        return dictDataService.updateDictData(dict);
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public void remove(@PathVariable(name = "dictCodes") Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(List.of(dictCodes));
    }
}
