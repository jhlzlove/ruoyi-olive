package com.olive.framework.filter;

import com.olive.framework.util.SecurityUtils;
import com.olive.framework.web.system.DataScopeProps;
import com.olive.framework.web.system.SysRole;
import com.olive.framework.web.system.SysUser;
import com.olive.framework.web.system.service.SysDeptService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.FilterArgs;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author jhlz
 * @version x.x.x
 */
@Component
@AllArgsConstructor
public class DataScopeFilter implements Filter<DataScopeProps> {

    private final SysDeptService deptService;
    private final JSqlClient sqlClient;

    @Override
    public void filter(FilterArgs<DataScopeProps> args) {
        DataScopeProps table = args.getTable();
        SysUser user = SecurityUtils.getLoginUser().getUser();
        List<SysRole> roles = user.roles();
        if (CollectionUtils.isEmpty(roles)) return;
        for (SysRole role : roles) {
            // 1: 全部数据权限
            // 2: 自定义数据权限
            // 3: 部门数据权限
            // 4: 部门及以下数据权限
            // 5: 仅本人数据权限
            String dataScope = role.dataScope();

            switch (dataScope) {
                case "1" -> {
                    break;
                }
                case "2" -> {
                    args.where(table.deptId().inIf(role.deptIds()));
                }
                case "3" -> {
                    args.where(table.dept().deptId().eq(user.deptId()));
                }
                case "4" -> {
                    // 查询用户部门和子部门
                    Collection<Long> deptIds = deptService.selectChildrenDeptByParentId(user.deptId());
                    args.where(table.dept().deptId().inIf(deptIds));
                }
                case "5" -> {
                    args.where(table.userId().eq(user.userId()));
                }
            }
        }

    }
}
