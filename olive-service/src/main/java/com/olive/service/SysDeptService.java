package com.olive.service;

import com.olive.model.*;
import com.olive.model.constant.AppConstant;
import com.olive.model.dto.FlatDeptView;
import com.olive.model.dto.SysDeptSearch;
import com.olive.model.exception.SysDeptException;
import com.olive.service.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.query.ConfigurableSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysDeptService {
    private static final Logger log = LoggerFactory.getLogger(SysDeptService.class);
    private final JSqlClient sqlClient;
    private final SysDeptTable table = SysDeptTable.$;


    public List<SysDept> list(SysDeptSearch search) {
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.delFlag().eq("0"))
                .select(table)
                .execute();
    }

    public void checkDeptDataScope(Long deptId) {
        Long userId = SecurityUtils.getUserId();
        if (!(Objects.nonNull(userId) && 1L == userId)) {
            List<SysDept> depts = sqlClient.createQuery(table)
                    .where(table.deptId().eq(deptId))
                    .where(table.delFlag().eq("0"))
                    .orderBy(table.deptId(), table.orderNum())
                    .select(table)
                    .execute();
            if (CollectionUtils.isEmpty(depts)) {
                throw SysDeptException.notPermission("没有权限访问部门数据！");
            }
        }
    }

    public FlatDeptView info(Long deptId) {
        SysDept sysDept = sqlClient.createQuery(table)
                .where(table.deptId().eq(deptId))
                .select(table.fetch(
                        Fetchers.SYS_DEPT_FETCHER
                                .allScalarFields()
                                .parentId()
                                .parent(Fetchers.SYS_DEPT_FETCHER
                                        .deptName()
                                )
                ))
                .fetchOne();

        return new FlatDeptView(sysDept);
    }



    @Transactional(rollbackFor = Exception.class)
    public int add(SysDept dept) {
        if (checkDeptNameUnique(dept)) {
            throw SysDeptException.deptNameExist("新增失败，部门名称已存在", dept.deptName());
        }
        return sqlClient.getEntities().save(dept).getTotalAffectedRowCount();
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysDept dept) {
        Long deptId = dept.deptId();
        checkDeptDataScope(deptId);
        if (checkDeptNameUnique(dept)) {
            throw SysDeptException.deptNameExist("修改部门失败，部门名称已存在", dept.deptName());
        } else if (dept.parentId().equals(deptId)) {
            throw SysDeptException.deptNameExist("修改部门失败，上级部门不能是自己", dept.deptName());
        } else if (Objects.equals(AppConstant.DEPT_DISABLE, dept.status()) && this.selectNormalChildrenDeptById(deptId) > 0) {
            throw SysDeptException.hasNormalChildDept("该部门包含未停用的子部门！");
        }
        return sqlClient.getEntities().save(dept).getTotalAffectedRowCount();
    }

    public int selectNormalChildrenDeptById(Long deptId) {
        // select count(*) from sys_dept where status = 0 and del_flag = '0' and find_in_set(#{deptId}, ancestors)
        return sqlClient.createQuery(table)
                .where(table.status().eq("0"))
                .where(table.delFlag().eq("0"))
                .where(Predicate.sql("find_in_set(%e, %v)",
                        it -> it.expression(table.ancestors())
                                .value(deptId)
                ))
                .select(table.count())
                .fetchOne().intValue();
    }

    public void delete(long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw SysDeptException.hasChildDeptNotAllowDelete("存在下级部门,不允许删除");
        }
        if (checkDeptExistUser(deptId)) {
            throw SysDeptException.hasUserNotAllowDelete("部门存在用户,不允许删除");
        }
        checkDeptDataScope(deptId);
        sqlClient.deleteById(SysDept.class, deptId);
    }

    public List<Long>  selectDeptListByRoleId(Long roleId) {
        SysRole role = sqlClient.findById(SysRole.class, roleId);

        // 子查询查询父部门 id
        ConfigurableSubQuery<Long> subQuery = sqlClient.createSubQuery(table)
                .where(table.roles(r -> r.roleId().eq(roleId)))
                .where(table.roles(r -> r.deptCheckStrictly().eqIf(role.deptCheckStrictly())))
                .select(table.parentId());

        List<Long> result = sqlClient.createQuery(table)
                .where(table.roles(r -> r.roleId().eq(roleId)))
                .where(table.deptId().notInIf(subQuery))
                .select(table.deptId())
                .execute();
        log.info("结果集 {}", result);
        return result;
    }

    private boolean hasChildByDeptId(long deptId) {
        return sqlClient.createQuery(table)
                .where(table.parentId().eq(deptId))
                .where(table.delFlag().eq("0"))
                .select(table.count())
                .fetchOne() > 0;
    }

    private boolean checkDeptExistUser(long deptId) {
        SysUserTable userTable = SysUserTable.$;
        return sqlClient.createQuery(userTable)
                .where(userTable.delFlag().eq("0"))
                .where(userTable.deptId().eq(deptId))
                .select(userTable.count())
                .fetchOne() > 0;
    }

    public List<TreeSelect> selectDeptTreeList(SysDept dept) {
        List<SysDept> depts = sqlClient.createQuery(table)
                .where(table.parentId().eq(0L))
                .select(table.fetch(
                        Fetchers.SYS_DEPT_FETCHER
                                .allScalarFields()
                                .recursiveChildren()
                ))
                .execute();
        return depts.stream().map(TreeSelect::new).toList();
    }

    /**
     * 获取所有子部门 id
     * @param deptId    父部门id
     * @return  子部门 id 集合
     */
    public Collection<Long> selectChildrenDeptByParentId(Long deptId) {
        Assert.notNull(deptId, "父部门 id 不能为 null");
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.status().eq("0"))
                .where(table.parentId().eq(deptId))
                .select(table.deptId())
                .execute();
    }


    /**
     * 部门名称唯一检查
     * @param dept  部门信息
     * @return  true ？ 已存在 ： 不存在
     */
    private boolean checkDeptNameUnique(SysDept dept) {
        return sqlClient.createQuery(table)
                .where(table.deptId().ne(dept.deptId()))
                .where(table.parentId().eq(dept.parentId()))
                .where(table.deptName().eq(dept.deptName()))
                .where(table.delFlag().eq("0"))
                .exists();
    }
}
