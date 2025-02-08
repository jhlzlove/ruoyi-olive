package com.olive.system.service;

import com.olive.framework.constant.Constants;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.TreeSelect;
import com.olive.framework.web.system.*;
import com.olive.framework.web.system.dto.SysMenuSearch;
import com.olive.system.domain.MetaVo;
import com.olive.system.domain.RouterVo;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.table.AssociationTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysMenuService {
    private static final Logger log = LoggerFactory.getLogger(SysMenuService.class);
    private final JSqlClient sqlClient;
    final SysMenuTable table = SysMenuTable.$;
    private final SysRoleService roleService;

    /**
     * 查询系统菜单列表
     *
     * @param search 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenuSearch search) {
        Long userId = SecurityUtils.getUserId();
        // 管理员显示所有菜单信息
        if (Objects.nonNull(userId) && 1L == userId) {
            return sqlClient.createQuery(table)
                    .where(search)
                    .orderBy(table.parentId(), table.orderNum())
                    .select(table)
                    .execute();
        } else {
            return sqlClient.createQuery(table)
                    .where(search)
                    .where(table.roles(role -> role.users().userId().eq(userId)))
                    .orderBy(table.parentId(), table.orderNum())
                    .select(table)
                    .execute();
        }
    }

    public Map<String, Object> treeList(long roleId) {
        List<SysMenu> result = sqlClient.createQuery(table)
                .where(table.parentId().eq(0L))
                .select(table.fetch(
                                Fetchers.SYS_MENU_FETCHER
                                        .parentId()
                                        .menuName()
                                        .recursiveChildren()
                        )
                )
                .execute();
        List<TreeSelect> list = result.stream().map(TreeSelect::new).toList();
        return Map.of("menus", list, "checkedKeys", selectMenuListByRoleId(roleId));
    }

    public List<SysMenu> selectMenuTreeByUserId(long userId) {

        List<SysMenu> result = sqlClient.createQuery(table)
                .where(table.menuType().in(List.of("M", "C")))
                .where(table.status().eq("0"))
                .where(table.roles(role -> role.status().eq("0")))
                .where(table.roles(role -> role.users().userId().eq(userId)))
                .orderBy(table.parentId(), table.orderNum())
                .select(table)
                .execute();
        return result;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> perms = sqlClient.createQuery(table)
                .where(table.status().eq("0"))
                .where(table.roles(role -> role.status().eq("0")))
                .where(table.roles(role -> role.users().userId().eq(userId)))
                .select(table.perms())
                .execute();
        log.info("查询结果： {}", perms);
        // List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        List<String> perms = sqlClient.createQuery(table)
                .where(table.roles(role -> role.roleId().eq(roleId)))
                .select(table.perms())
                .execute();
        log.info("根据 roleId 获取的权限列表 {}", perms);
        // List<String> perms = menuMapper.selectMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    public List<Long> selectMenuListByRoleId(Long roleId) {
        // com.olive.framework.web.system.SysRole temp = sqlClient.findById(com.olive.framework.web.system.SysRole.class, roleId);
        // return sqlClient.createQuery(table)
        //         .where(table.roles(role -> role.menuCheckStrictly().eqIf(temp.menuCheckStrictly())))
        //         .select(table.menuId())
        //         .execute();
        SysRole role = sqlClient.findById(SysRole.class, roleId);
        // SysRole role = roleMapper.selectRoleById(roleId);
        // select m.menu_id
        // from sys_menu m
        // left join sys_role_menu rm on m.menu_id = rm.menu_id
        // where rm.role_id = #{roleId}
        //     <if test="menuCheckStrictly">
        //         and m.menu_id not in (select m.parent_id from sys_menu m inner join sys_role_menu rm on m.menu_id = rm.menu_id and rm.role_id = #{roleId})
        //     </if>
        // order by m.parent_id, m.order_num
        return sqlClient.createQuery(table)
                .where(table.roles(r -> r.roleId().eq(roleId)))
                .whereIf(Objects.nonNull(role) && role.menuCheckStrictly(),
                        table.menuId().notInIf(
                                sqlClient.createSubQuery(table)
                                        .where(table.roles(r -> r.roleId().eq(roleId)))
                                        .select(table.parentId())
                        ))
                .orderBy(table.parentId(), table.orderNum())
                .select(table.menuId())
                .execute();
        // return menuMapper.selectMenuListByRoleId(roleId, role.isMenuCheckStrictly());
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildMenuTreeSelect(SysMenuSearch search) {
        List<SysMenu> sysMenus = selectMenuList(search);
        return sysMenus.stream().map(TreeSelect::new).toList();
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    public SysMenu selectMenuById(long menuId) {
        return sqlClient.findById(SysMenu.class, menuId);
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public boolean hasChildByMenuId(Long menuId) {
        return sqlClient.createQuery(table)
                .where(table.parentId().eq(menuId))
                .select(table.count())
                .fetchOne() > 0;
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public boolean checkMenuExistRole(Long menuId) {
        AssociationTable<SysMenu, SysMenuTableEx, SysRole, SysRoleTableEx> association =
                AssociationTable.of(SysMenuTableEx.class, SysMenuTableEx::roles);
        var list = sqlClient.createAssociationQuery(association)
                .where(association.source().menuId().eq(menuId))
                .select(association)
                .execute();
        return !list.isEmpty();
    }

    /**
     * 新增保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Transactional
    public int insertMenu(SysMenu menu) {
        return sqlClient.save(menu).getTotalAffectedRowCount();
    }

    /**
     * 修改保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Transactional
    public int updateMenu(SysMenu menu) {
        return sqlClient.save(menu).getTotalAffectedRowCount();
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Transactional
    public int deleteMenuById(Long menuId) {
        return sqlClient.deleteById(SysMenu.class, menuId).getTotalAffectedRowCount();
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean checkMenuNameUnique(SysMenu menu) {
        long menuId = ImmutableObjects.isLoaded(menu, SysMenuProps.MENU_ID) ? menu.menuId() : -1L;
        SysMenu info = sqlClient.createQuery(table)
                .where(table.parentId().eq(menu.parentId()))
                .where(table.menuName().eq(menu.menuName()))
                .select(table)
                .limit(1)
                .fetchOneOrNull();
        log.debug("查询到的对象: {}", info);
        if (StringUtils.isNotNull(info) && info.menuId() != menuId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    private String getRouteName(SysMenu menu) {
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu)) {
            return StringUtils.EMPTY;
        }
        return getRouteName(menu.routeName(), menu.path());
    }

    /**
     * 获取路由名称，如没有配置路由名称则取路由地址
     *
     * @param name 路由名称
     * @param path 路由地址
     * @return 路由名称（驼峰格式）
     */
    private String getRouteName(String name, String path) {
        String routerName = StringUtils.isNotEmpty(name) ? name : path;
        return StringUtils.capitalize(routerName);
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    private String getRouterPath(SysMenu menu) {
        String routerPath = menu.path();
        if (ImmutableObjects.isLoaded(menu, SysMenuProps.PARENT)) {
            // 内链打开外网方式
            if (menu.parentId().intValue() != 0 && isInnerLink(menu)) {
                routerPath = innerLinkReplaceEach(routerPath);
            }
            // 非外链并且是一级目录（类型为目录）
            if (0 == menu.parentId().intValue() && UserConstants.TYPE_DIR.equals(menu.menuType())
                    && UserConstants.NO_FRAME.equals(menu.frameFlag())) {
                routerPath = "/" + menu.path();
            }
        }

        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu)) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenu menu) {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.component()) && !isMenuFrame(menu)) {
            component = menu.component();
        } else if (StringUtils.isEmpty(menu.component()) && ImmutableObjects.isLoaded(menu, SysMenuProps.PARENT) && menu.parentId().intValue() != 0
                && isInnerLink(menu)) {
            component = UserConstants.INNER_LINK;
        } else if (StringUtils.isEmpty(menu.component()) && isParentView(menu)) {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isMenuFrame(SysMenu menu) {
        if (ImmutableObjects.isLoaded(menu, SysMenuProps.PARENT)) {
            return menu.parentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.menuType())
                    && menu.frameFlag().equals(UserConstants.NO_FRAME);
        }
        return false;
    }

    /**
     * 是否为内链组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isInnerLink(SysMenu menu) {
        return menu.frameFlag().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.path());
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isParentView(SysMenu menu) {
        return ImmutableObjects.isLoaded(menu, SysMenuProps.PARENT) && menu.parentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.menuType());
    }

    /**
     * 内链域名特殊字符替换
     *
     * @return 替换后的内链域名
     */
    private String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[]{Constants.HTTP, Constants.HTTPS, Constants.WWW, "."},
                new String[]{"", "", "", "/"});
    }

    public List<RouterVo> buildRouters() {
        return buildMenus(selectMenuTreeByUserId());
    }

    /**
     * 根据用户 id 获取菜单列表
     *
     * @return 菜单集合
     */
    private List<SysMenu> selectMenuTreeByUserId() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = null;
        // 管理员查询全部
        if (SecurityUtils.isAdmin(userId)) {
            menus = sqlClient.createQuery(table)
                    .where(table.parentId().eq(0L))
                    .where(table.menuType().in(List.of("M", "C")))
                    .where(table.status().eq("0"))
                    .orderBy(table.parentId(), table.orderNum())
                    .select(table.fetch(
                            Fetchers.SYS_MENU_FETCHER
                                    .allScalarFields()
                                    .parentId()
                                    .recursiveChildren()
                    ))
                    .distinct()
                    .execute();
        } else {
            menus = sqlClient.createQuery(table)
                    .where(table.parentId().eq(0L))
                    .where(table.asTableEx().roles().users().userId().eq(userId))
                    .where(table.menuType().in(List.of("M", "C")))
                    .where(table.status().eq("0"))
                    .where(table.asTableEx().roles().status().eq("0"))
                    .orderBy(table.parentId(), table.orderNum())
                    .select(table.fetch(
                            Fetchers.SYS_MENU_FETCHER
                                    .allScalarFields()
                                    .parentId()
                                    .recursiveChildren()
                    ))
                    .distinct()
                    .execute();
        }
        log.info("查询结果：{}", menus);
        return menus;
    }

    /**
     * 构建前端路由所需要的菜单
     *
     * @return 路由列表
     */
    private List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.visible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.query());
            router.setMeta(new MetaVo(menu.menuName(), menu.icon(), StringUtils.equals("1", menu.cacheFlag()),
                    menu.path()));
            List<SysMenu> cMenus = menu.children();
            if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.menuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (isMenuFrame(menu)) {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.path());
                children.setComponent(menu.component());
                children.setName(getRouteName(menu.routeName(), menu.path()));
                children.setMeta(new MetaVo(menu.menuName(), menu.icon(),
                        StringUtils.equals("1", menu.cacheFlag()), menu.path()));
                children.setQuery(menu.query());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (ImmutableObjects.isLoaded(menu, SysMenuProps.PARENT) && menu.parentId().intValue() == 0 && isInnerLink(menu)) {
                router.setMeta(new MetaVo(menu.menuName(), menu.icon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.path());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(getRouteName(menu.routeName(), routerPath));
                children.setMeta(new MetaVo(menu.menuName(), menu.icon(), menu.path()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }
}
