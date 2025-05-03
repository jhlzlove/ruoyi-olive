package com.olive.api.system;

import com.olive.service.aop.log.Log;
import com.olive.model.SysRole;
import com.olive.model.SysUser;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysRoleSearch;
import com.olive.model.record.PageQuery;
import com.olive.model.record.SysUserCondition;
import com.olive.service.*;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色信息
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController {
    private static final Logger log = LoggerFactory.getLogger(SysRoleController.class);
    private final TokenService tokenService;
    private final SysPermissionService permissionService;
    private final SysDeptService sysDeptService;
    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public Page<SysRole> list(SysRoleSearch search, PageQuery page) {
        return  sysRoleService.page(search, page);
    }

    // @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    // @PreAuthorize("@ss.hasPermi('system:role:export')")
    // @PostMapping("/export")
    // public void export(HttpServletResponse response, SysRole role) {
    //     List<SysRole> list = roleService.selectRoleList(role);
    //     ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
    //     util.exportExcel(response, list, "角色数据");
    // }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public SysRole getInfo(@PathVariable(name = "roleId") long roleId) {
        sysRoleService.checkRoleDataScope(roleId);
        return sysRoleService.info(roleId);
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysRole role) {
        return sysRoleService.add(role);

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysRole role) {
        return sysRoleService.update(role);
    }

    /**
     * 修改保存数据权限
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public void dataScope(@RequestBody SysRole role) {
        sysRoleService.checkRoleAllowed(role);
        sysRoleService.checkRoleDataScope(role.roleId());
        sysRoleService.authDataScope(role);
        // return roleService.authDataScope(role);
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public boolean changeStatus(@RequestBody SysRole role) {
        sysRoleService.checkRoleAllowed(role);
        sysRoleService.checkRoleDataScope(role.roleId());
        return sysRoleService.update(role);
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public void remove(@PathVariable(name = "roleIds") Long[] roleIds) {
        sysRoleService.deleteRoleByIds(List.of(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    public List<SysRole> optionselect() {
        return sysRoleService.list();
    }


    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public Page<SysUser> allocatedList(SysUserCondition user) {
        return sysUserService.selectAllocatedList(user);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public Page<SysUser> unallocatedList(SysUserCondition user) {
        return sysUserService.selectUnallocatedList(user);
    }

    /**
     * 取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public boolean cancelAuthUser(@RequestBody SysUser user) {
        return sysRoleService.deleteAuthUser(user);
        // return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public boolean cancelAuthUserAll(@RequestParam(required = false) Long roleId,
                                        @RequestParam(required = false) Long[] userIds) {
        return sysRoleService.deleteAuthUsers(roleId, userIds);
        // return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public boolean selectAuthUserAll(@RequestParam(required = false)Long roleId,
                                        @RequestParam(required = false) Long[] userIds) {
        sysRoleService.checkRoleDataScope(roleId);
        return sysRoleService.insertAuthUsers(roleId, userIds);
    }

    /**
     * 获取对应角色部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/deptTree/{roleId}")
    public Map<String, Object> deptTree(@PathVariable("roleId") long roleId) {
        List<Long> value = sysDeptService.selectDeptListByRoleId(roleId);
        return Map.of(
                "checkedKeys", value,
                "depts", sysDeptService.selectDeptTreeList(null)
        );
    }
}
