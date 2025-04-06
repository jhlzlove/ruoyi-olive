package com.olive.api.system;

import com.olive.base.response.R;
import com.olive.framework.log.Log;
import com.olive.model.SysDept;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.FlatDeptView;
import com.olive.model.dto.SysDeptSearch;
import com.olive.service.SysDeptService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门信息
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController {
    private final SysDeptService deptService;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public List<SysDept> list(SysDeptSearch search) {
        return deptService.list(search);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public R excludeChild(@PathVariable(value = "deptId") long deptId) {
        List<SysDept> depts = deptService.list(null);
        depts.removeIf(d -> d.deptId() == deptId || ArrayUtils.contains(StringUtils.split(d.ancestors(), ","), deptId + ""));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public FlatDeptView getInfo(@PathVariable(name = "deptId") long deptId) {
        deptService.checkDeptDataScope(deptId);
        return deptService.info(deptId);
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public int add(@Validated @RequestBody SysDept dept) {
        return deptService.add(dept);
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit(@Validated @RequestBody SysDept dept) {
        return deptService.update(dept);
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public void remove(@PathVariable(name = "deptId") long deptId) {
        deptService.delete(deptId);
    }
}
