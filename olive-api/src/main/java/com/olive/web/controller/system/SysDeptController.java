package com.olive.web.controller.system;

import com.olive.common.response.R;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.enums.BusinessType;
import com.olive.framework.exception.CustomException;
import com.olive.framework.log.Log;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.SysDept;
import com.olive.framework.web.system.dto.FlatDeptView;
import com.olive.framework.web.system.dto.SysDeptSearch;
import com.olive.framework.web.system.service.SysDeptService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
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
    private final SysDeptService deptService2;

    /**
     * 获取部门列表
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public List<SysDept> list(SysDeptSearch search) {
        return deptService2.list(search);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list/exclude/{deptId}")
    public R excludeChild(@PathVariable(value = "deptId") long deptId) {
        List<SysDept> depts = deptService2.list(null);
        depts.removeIf(d -> d.deptId() == deptId || ArrayUtils.contains(StringUtils.split(d.ancestors(), ","), deptId + ""));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @GetMapping(value = "/{deptId}")
    public FlatDeptView getInfo(@PathVariable(name = "deptId") long deptId) {
        deptService2.checkDeptDataScope(deptId);
        return deptService2.selectDeptById(deptId);
        //
        // deptService.checkDeptDataScope(deptId);
        // return success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public int add(@Validated @RequestBody com.olive.framework.web.system.SysDept dept) {
        if (!deptService2.checkDeptNameUnique(dept)) {
            throw new CustomException("新增部门'" + dept.deptName() + "'失败，部门名称已存在");
        }
        return deptService2.insertDept(dept);
    }

    /**
     * 修改部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit(@Validated @RequestBody SysDept dept) {
        Long deptId = dept.deptId();
        deptService2.checkDeptDataScope(deptId);
        if (!deptService2.checkDeptNameUnique(dept)) {
            throw new CustomException("修改部门'" + dept.deptName() + "'失败，部门名称已存在");
        } else if (dept.parentId().equals(deptId)) {
            throw new CustomException("修改部门'" + dept.deptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.status()) && deptService2.selectNormalChildrenDeptById(deptId) > 0) {
            throw new CustomException("该部门包含未停用的子部门！");
        }
        return deptService2.updateDept(dept);
    }

    /**
     * 删除部门
     */
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public void remove(@PathVariable(name = "deptId") long deptId) {
        deptService2.deleteDeptById(deptId);
    }
}
