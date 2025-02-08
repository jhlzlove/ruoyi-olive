package com.olive.framework.web.system.service;

import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.CustomException;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.*;
import com.olive.framework.web.system.dto.FlatDeptView;
import com.olive.framework.web.system.dto.SysDeptSearch;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.ImmutableObjects;
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
            if (StringUtils.isEmpty(depts)) {
                throw new ServiceException("没有权限访问部门数据！");
            }
        }
    }

    public FlatDeptView selectDeptById(Long deptId) {
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

    public boolean checkDeptNameUnique(SysDept dept) {
        long deptId = ImmutableObjects.isLoaded(dept, SysDeptProps.DEPT_ID) ? dept.deptId() : -1L;
        SysDept info = sqlClient.createQuery(table)
                .where(table.parentId().eq(dept.parentId()))
                .where(table.deptName().eq(dept.deptName()))
                .where(table.delFlag().eq("0"))
                .select(table)
                .limit(1)
                .fetchOneOrNull();
        if (Objects.nonNull(info) && info.deptId() != deptId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertDept(SysDept dept) {
        return sqlClient.insert(dept).getTotalAffectedRowCount();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateDept(SysDept dept) {
        return sqlClient.save(dept).getTotalAffectedRowCount();
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

    public void deleteDeptById(long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new CustomException("存在下级部门,不允许删除");
        }
        if (checkDeptExistUser(deptId)) {
            throw new CustomException("部门存在用户,不允许删除");
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
}
