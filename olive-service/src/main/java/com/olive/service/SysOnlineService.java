package com.olive.service;

import com.olive.model.*;
import com.olive.model.record.SysUserOnline;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 在线用户监控
 *
 * @author ruoyi
 */
@Service
@AllArgsConstructor
public class SysOnlineService {
    private final JSqlClient sqlClient;

    /**
     * 通过登录地址查询信息
     *
     * @param ipaddr 登录地址
     * @param user   用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过用户名称查询信息
     *
     * @param userName 用户名称
     * @param user     用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByUserName(String userName, LoginUser user) {
        if (StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过登录地址/用户名称查询信息
     *
     * @param ipaddr   登录地址
     * @param userName 用户名称
     * @param user     用户信息
     * @return 在线用户信息
     */
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, LoginUser user) {
        if (StringUtils.equals(ipaddr, user.getIpaddr()) && StringUtils.equals(userName, user.getUsername())) {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 设置在线用户信息
     *
     * @param user 用户信息
     * @return 在线用户
     */
    public SysUserOnline loginUserToUserOnline(LoginUser user) {
        if (Objects.isNull(user) || Objects.isNull(user.getUser())) {
            return null;
        }
        String deptName = null;
        SysUserTable table = SysUserTable.$;
        SysUser temp = sqlClient.createQuery(table)
                .where(table.userId().eq(user.getUserId()))
                .select(table.fetch(
                        SysUserFetcher.$.dept(SysDeptFetcher.$.deptName())
                ))
                .fetchOneOrNull();
        if (temp != null) {
            deptName = temp.dept().deptName();
        }
        return new SysUserOnline(
                user.getToken(),
                deptName,
                user.getUsername(),
                user.getIpaddr(),
                user.getLoginLocation(),
                user.getBrowser(),
                user.getOs(),
                user.getLoginTime()
        );
    }
}
