package com.olive.web.controller.system;

import com.olive.common.response.R;
import com.olive.framework.enums.BusinessType;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.log.Log;
import com.olive.framework.record.PageQuery;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.*;
import com.olive.framework.web.system.dto.SysUserSearch;
import com.olive.framework.web.system.service.SysDeptService;
import com.olive.system.service.SysPostService;
import com.olive.system.service.SysRoleService;
import com.olive.system.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.babyfish.jimmer.DraftObjects;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response) {
        List<SysUser> all = userService.findAll();
        try (XSSFWorkbook workbook = new XSSFWorkbook();) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow headRow = sheet.createRow(0);
            headRow.createCell(0).setCellValue("用户名称");
            headRow.createCell(1).setCellValue("用户昵称");
            headRow.createCell(2).setCellValue("部门");
            headRow.createCell(3).setCellValue("手机号码");
            headRow.createCell(4).setCellValue("状态");

            Collection<ImmutableProp> values = ImmutableType.get(SysUser.class).getProps().values();
            for (int i = 0; i < all.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                SysUser user = all.get(i);
                values.forEach(v -> {
                    ImmutableSpi e = (ImmutableSpi) user;
                    PropId id = v.getId();
                    if (e.__isLoaded(id)) {
                        Object o = e.__get(id);
                        System.out.println(
                                "index name is ========== " + v.getName()
                                        + ", value is ======== " + o
                        );
                        int index = switch (v.getName()) {
                            case "userName" -> 0;
                            case "nickName" -> 1;
                            case "dept" -> 2;
                            case "phonenumber" -> 3;
                            case "status" -> 4;
                            default -> -1;
                        };
                        if (Objects.nonNull(o) && !Objects.equals(-1, index)) {
                            if (o instanceof SysDept dept) {
                                row.createCell(index).setCellValue(dept.deptName());
                            } else {
                                row.createCell(index).setCellValue(o.toString());
                            }
                        }
                    }
                });
            }
            // xls
            // response.setContentType("application/vnd.ms-excel");
            // xlsx
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx");
            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        List<SysRole> roles = roleService.selectRoleAll();
        List<SysRole> roleList = 1L == userId ? roles : roles.stream().filter(r -> r.roleId() != 1L).collect(Collectors.toList());
        List<SysPost> posts = postService.selectPostAll();
        SysUser sysUser = userService.selectUserById(userId);
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
        if (!userService.checkUserNameUnique(user.userName())) {
            throw new ServiceException("新增用户'" + user.userName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.phonenumber()) && !userService.checkPhoneUnique(user)) {
            throw new ServiceException("新增用户'" + user.userName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.email()) && !userService.checkEmailUnique(user)) {
            throw new ServiceException("新增用户'" + user.userName() + "'失败，邮箱账号已存在");
        }
        SysUser target = Immutables.createSysUser(user, draft -> {
            DraftObjects.set(draft, SysUserProps.PASSWORD, SecurityUtils.encryptPassword(user.password()));
        });
        return userService.insertUser(target);
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.userId());
        if (!userService.checkUserNameUnique(user.userName())) {
            throw new ServiceException("修改用户'" + user.userName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.phonenumber()) && !userService.checkPhoneUnique(user)) {
            throw new ServiceException("修改用户'" + user.userName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.email()) && !userService.checkEmailUnique(user)) {
            throw new ServiceException("修改用户'" + user.userName() + "'失败，邮箱账号已存在");
        }
        // user.setUpdateBy(getUsername());
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public boolean remove(@PathVariable(name = "userIds") Long[] userIds) {
        if (ArrayUtils.contains(userIds, SecurityUtils.getUserId())) {
            throw new ServiceException("当前用户不能删除");
        }
        return userService.deleteUserByIds(List.of(userIds));
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
        return userService.updateUser(temp);
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
        return userService.updateUser(user);
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public Map<String, Object> authRole(@PathVariable("userId") long userId) {
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        List<SysRole> roleList = userId == 1L ? roles : roles.stream().filter(r -> r.roleId() != 1).collect(Collectors.toList());
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
