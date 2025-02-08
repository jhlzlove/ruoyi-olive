package com.olive.system.service;

import com.olive.framework.enums.UserStatus;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.web.system.Immutables;
import com.olive.framework.web.system.LoginUser;
import com.olive.framework.web.system.SysUser;
import com.olive.framework.web.system.service.SysPasswordService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * 用户验证处理
 *
 * @author ruoyi
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final SysUserService userService;
    private final SysPasswordService passwordService;
    private final SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName(username);
        if (Objects.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(user.delFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.status())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user) {
        return new LoginUser(user.userId(), user.deptId(), user, permissionService.getMenuPermission(user));
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        SysUser sysUser = userService.selectUserByUserName(user.getUsername());

        SysUser temp = Immutables.createSysUser(sysUser, draft -> {
            draft.setPassword(newPassword);
        });
        userService.updateUser(temp);
        return createLoginUser(temp);
    }
}
