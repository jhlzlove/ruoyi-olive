package com.olive.web.controller.system;

import com.olive.framework.enums.BusinessType;
import com.olive.framework.exception.CustomException;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.framework.record.SysUserCondition;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.LoginUser;
import com.olive.framework.web.system.SysRole;
import com.olive.framework.web.system.SysUser;
import com.olive.framework.web.system.dto.SysRoleSearch;
import com.olive.framework.web.system.service.SysDeptService;
import com.olive.framework.web.system.service.TokenService;
import com.olive.system.service.SysPermissionService;
import com.olive.system.service.SysRoleService;
import com.olive.system.service.SysUserService;
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
    public Page<com.olive.framework.web.system.SysRole> list(SysRoleSearch search, PageQuery page) {
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
        return sysRoleService.selectRoleById(roleId);
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysRole role) {
        if (!sysRoleService.checkRoleNameUnique(role)) {
            throw new CustomException("新增角色'" + role.roleName() + "'失败，角色名称已存在");
        }
        else if (!sysRoleService.checkRoleKeyUnique(role)) {
            throw new CustomException("新增角色'" + role.roleName() + "'失败，角色权限已存在");
        }
        // role.setCreateBy(getUsername());
        return sysRoleService.insertRole(role) > 0;

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public void edit(@Validated @RequestBody SysRole role) {
        sysRoleService.checkRoleAllowed(role);
        sysRoleService.checkRoleDataScope(role.roleId());
        if (!sysRoleService.checkRoleNameUnique(role)) {
            throw new CustomException("修改角色'" + role.roleName() + "'失败，角色名称已存在");
        } else if (!sysRoleService.checkRoleKeyUnique(role)) {
            throw new CustomException("修改角色'" + role.roleName() + "'失败，角色权限已存在");
        }
        if (sysRoleService.updateRole(role) > 0) {
            // 更新缓存用户权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (StringUtils.isNotNull(loginUser.getUser()) && !(loginUser.getUser().userId() ==1L)) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                // loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                String userName = loginUser.getUser().userName();
                loginUser.setUser(sysUserService.selectUserByUserName(userName));
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                tokenService.setLoginUser(loginUser);
            }
        }
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
    public int changeStatus(@RequestBody SysRole role) {
        sysRoleService.checkRoleAllowed(role);
        sysRoleService.checkRoleDataScope(role.roleId());
        return sysRoleService.updateRole(role);
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
        // startPage();
        // List<SysUser> list = userService.selectAllocatedList(user);
        // return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public Page<SysUser> unallocatedList(SysUserCondition user) {
        return sysUserService.selectUnallocatedList(user);
        // startPage();
        // List<SysUser> list = userService.selectUnallocatedList(user);
        // return getDataTable(list);
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
