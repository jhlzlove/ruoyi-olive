package com.olive.framework.filter;

import com.olive.model.DataScopeProps;
import com.olive.model.LoginUser;
import com.olive.model.SysRole;
import com.olive.model.SysUser;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.FilterArgs;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jhlz
 * @version x.x.x
 */
@Component
@AllArgsConstructor
public class DataScopeFilter implements Filter<DataScopeProps> {

    @Override
    public void filter(FilterArgs<DataScopeProps> args) {
        DataScopeProps table = args.getTable();
        // 获取当前登录用户
        SysUser user = ((LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
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
                    args.where(Predicate.sql("find_in_set(%v, %e)", it ->
                            it.expression(table.dept().deptId()).value(user.deptId())
                    ));
                }
                case "5" -> {
                    args.where(table.userId().eq(user.userId()));
                }
            }
        }

    }
}
