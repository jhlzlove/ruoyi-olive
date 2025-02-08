package com.olive.framework.web.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Treeselect树结构实体类
 *
 * @author ruoyi
 */
@Data
public class TreeSelect implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 父节点ID
     */
    private Long parentId;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect() {

    }

    // public TreeSelect(SysDept dept) {
    //     this.id = dept.getDeptId();
    //     this.parentId = dept.getParentId();
    //     this.label = dept.getDeptName();
    //     this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    // }
    //
    // public TreeSelect(SysMenu menu) {
    //     this.id = menu.getMenuId();
    //     this.label = menu.getMenuName();
    //     this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    // }

    public TreeSelect(SysMenu menu) {
        this.id = menu.menuId();
        this.label = menu.menuName();
        if (Objects.nonNull(menu.children())) {
            this.children = menu.children().stream().map(TreeSelect::new).collect(Collectors.toList());
        }
    }

    public TreeSelect(SysDept dept) {
        this.id = dept.deptId();
        this.label = dept.deptName();
        if (Objects.nonNull(dept.children())) {
            this.children = dept.children().stream().map(TreeSelect::new).collect(Collectors.toList());
        }
    }
}
