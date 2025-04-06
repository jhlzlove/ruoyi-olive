package com.olive.api.system;

import com.olive.framework.log.Log;
import com.olive.model.SysDictData;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysDictDataSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.DictDataService;
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
@RequestMapping("/system/dict/data")
@AllArgsConstructor
public class SysDictDataController {
   private final DictDataService dictDataService;
   private final DictTypeService dictTypeService;

   @PreAuthorize("@ss.hasPermi('system:dict:list')")
   @GetMapping("/list")
   public Page<SysDictData> list(SysDictDataSearch search, PageQuery pageRecord) {
       return dictDataService.page(search, pageRecord);
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
       return dictDataService.getByDictCode(dictCode);
   }

   /**
    * 根据字典类型查询字典数据信息
    */
   @GetMapping(value = "/type/{dictType}")
   public List<SysDictData> dictType(@PathVariable(name = "dictType") String dictType) {
       return  dictTypeService.listByDictType(dictType);
   }

   /**
    * 新增字典类型
    */
   @PreAuthorize("@ss.hasPermi('system:dict:add')")
   @Log(title = "字典数据", businessType = BusinessType.INSERT)
   @PostMapping
   public boolean add(@Validated @RequestBody SysDictData dict) {
       return dictDataService.add(dict);
   }

   /**
    * 修改保存字典类型
    */
   @PreAuthorize("@ss.hasPermi('system:dict:edit')")
   @Log(title = "字典数据", businessType = BusinessType.UPDATE)
   @PutMapping
   public boolean edit(@Validated @RequestBody SysDictData dict) {
       return dictDataService.update(dict);
   }

   /**
    * 删除字典类型
    */
   @PreAuthorize("@ss.hasPermi('system:dict:remove')")
   @Log(title = "字典类型", businessType = BusinessType.DELETE)
   @DeleteMapping("/{dictCodes}")
   public void remove(@PathVariable(name = "dictCodes") List<Long> dictCodes) {
       dictDataService.delete(dictCodes);
   }
}
