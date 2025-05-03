package com.olive.api.system;

import com.olive.service.aop.log.Log;
import com.olive.model.SysMenu;
import com.olive.model.TreeSelect;
import com.olive.model.constant.BusinessType;
import com.olive.model.dto.SysMenuSearch;
import com.olive.service.SysMenuService;
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
        return menuService.list(search);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public SysMenu getInfo(@PathVariable(name = "menuId") long menuId) {
        return menuService.info(menuId);
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
    public boolean add(@Validated @RequestBody SysMenu menu) {

        return menuService.add(menu);
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean edit( @RequestBody SysMenu menu) {

        return menuService.update(menu);
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public boolean remove(@PathVariable("menuId") long menuId) {

        return menuService.delete(menuId);
    }
}