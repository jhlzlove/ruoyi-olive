package com.olive.framework.manager.factory;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.constant.Constants;
import com.olive.framework.util.SpringUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.util.ip.AddressUtils;
import com.olive.framework.util.ip.IpUtils;
import com.olive.framework.web.system.SysLoginLog;
import com.olive.framework.web.system.SysLoginLogDraft;
import com.olive.framework.web.system.SysOperLog;
import eu.bitwalker.useragentutils.UserAgent;
import org.babyfish.jimmer.sql.JSqlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @author ruoyi
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
                                             final Object... args) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        final UserAgent userAgent = UserAgent.parseUserAgentString(requestAttributes.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(getBlock(ip));
                s.append(address);
                s.append(getBlock(username));
                s.append(getBlock(status));
                s.append(getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLoginLog loginLog = SysLoginLogDraft.$.produce(draft -> {
                    draft.setUserName(username)
                            .setIpaddr(ip)
                            .setLoginLocation(address)
                            .setBrowser(browser)
                            .setOs(os)
                            .setMsg(message)
                            .setLoginTime(LocalDateUtil.dateTime());
                    if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                        draft.setStatus(Constants.SUCCESS);
                    } else if (Constants.LOGIN_FAIL.equals(status)) {
                        draft.setStatus(Constants.FAIL);
                    }
                });
                // 无脑插入即可，日志啊，哥们儿
                JSqlClient sqlClient = SpringUtils.getBean(JSqlClient.class);
                sqlClient.insert(loginLog);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(JSqlClient.class).insert(operLog);
            }
        };
    }

    public static String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
