package com.olive.web.controller.system;

import com.olive.framework.constant.UserConstants;
import com.olive.framework.enums.BusinessType;
import com.olive.framework.exception.CustomException;
import com.olive.framework.log.Log;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.TreeSelect;
import com.olive.framework.web.system.SysMenu;
import com.olive.framework.web.system.dto.SysMenuSearch;
import com.olive.system.service.SysMenuService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单信息
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/menu")
public class SysMenuController {
    private final SysMenuService menuService;

    /**
     * 查询菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public List<SysMenu> list(SysMenuSearch search) {
        return menuService.selectMenuList(search);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public SysMenu getInfo(@PathVariable(name = "menuId") long menuId) {
        return menuService.selectMenuById(menuId);
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public List<TreeSelect> treeselect(SysMenuSearch search) {
        return menuService.buildMenuTreeSelect(search);
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public Map<String, Object> roleMenuTreeselect(@PathVariable("roleId") long roleId) {
        return menuService.treeList(roleId);
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public int add(@Validated @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            throw new CustomException("新增菜单'" + menu.menuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.frameFlag()) && !StringUtils.ishttp(menu.path())) {
            throw new CustomException("新增菜单'" + menu.menuName() + "'失败，地址必须以http(s)://开头");
        }
        return menuService.insertMenu(menu);
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public int edit( @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            throw new CustomException("修改菜单'" + menu.menuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.frameFlag()) && !StringUtils.ishttp(menu.path())) {
            throw new CustomException("修改菜单'" + menu.menuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.menuId() == menu.parentId()) {
            throw new CustomException("修改菜单'" + menu.menuName() + "'失败，上级菜单不能选择自己");
        }
        return menuService.updateMenu(menu);
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public int remove(@PathVariable("menuId") long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            throw new CustomException("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            throw new CustomException("菜单已分配,不允许删除");
        }
        return menuService.deleteMenuById(menuId);
    }
}