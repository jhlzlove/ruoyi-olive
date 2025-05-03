package com.olive.api.system;

import com.olive.base.response.R;
import com.olive.service.aop.log.Log;
import com.olive.model.*;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysUserSearch;
import com.olive.model.record.PageQuery;
import com.olive.service.SysDeptService;
import com.olive.service.SysPostService;
import com.olive.service.SysRoleService;
import com.olive.service.SysUserService;
import com.olive.service.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.DraftObjects;
import org.babyfish.jimmer.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
@AllArgsConstructor
public class SysUserController {

    private final SysPostService postService;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysDeptService deptService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public Page<SysUser> list(SysUserSearch search,
                              @RequestParam(required = false) Long deptId, PageQuery page) {
        return userService.page(search, deptId, page);
    }

    // @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    // @PreAuthorize("@ss.hasPermi('system:user:import')")
    // @PostMapping("/importData")
    // public R importData(@RequestPart MultipartFile file,
    //                              @RequestParam(required = false) boolean updateSupport) throws Exception {
    //     ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
    //     List<SysUser> userList = util.importExcel(file.getInputStream());
    //     String operName = getUsername();
    //     String message = userService.importUser(userList, updateSupport, operName);
    //     return R.ok(message);
    // }
    //
    // @PostMapping("/importTemplate")
    // public void importTemplate(HttpServletResponse response) {
    //     ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
    //     util.importTemplateExcel(response, "用户数据");
    // }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/{userId}")
    public Map<String, Object> getInfo(@PathVariable(value = "userId") long userId) {
        userService.checkUserDataScope(userId);
        List<SysRole> roles = roleService.list();
        List<SysRole> roleList = 1L == userId ? roles : roles.stream().filter(r -> r.roleId() != 1L).collect(Collectors.toList());
        List<SysPost> posts = postService.list();
        SysUser sysUser = userService.info(userId);
        List<Long> postIds = postService.selectPostListByUserId(userId);

        return Map.of(
                "data", sysUser,
                "roles", roleList,
                "posts", posts,
                "postIds", postIds,
                "roleIds", sysUser.roleIds()
        );
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public boolean add(@Validated @RequestBody SysUser user) {

        return userService.add(user);
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysUser user) {
        return userService.update(user);
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public boolean remove(@PathVariable(name = "ids") List<Long> ids) {

        return userService.delete(ids);
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public boolean resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.userId());
        SysUser temp = Immutables.createSysUser(user, draft -> {
            DraftObjects.set(draft, SysUserProps.PASSWORD, SecurityUtils.encryptPassword(user.password()));
        });
        return userService.update(temp);
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public boolean changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.userId());
        return userService.update(user);
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public Map<String, Object> authRole(@PathVariable("userId") long userId) {
        SysUser user = userService.info(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        List<SysRole> roleList = userId == 1L ? roles : roles.stream().filter(r -> r.roleId() != 1).toList();
        return Map.of(
                "user", user,
                "roles", roleList
        );
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public boolean insertAuthRole(@RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return true;
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public R deptTree(SysDept dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }
}
