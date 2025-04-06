package com.olive.service.manager.factory;

import com.olive.base.utils.LocalDateUtil;
import com.olive.model.SysLoginLog;
import com.olive.model.SysLoginLogDraft;
import com.olive.model.SysOperLog;
import com.olive.model.constant.AppConstant;
import com.olive.service.util.SpringUtils;
import com.olive.service.util.ip.AddressUtils;
import com.olive.service.util.ip.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
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
                String s = getBlock(ip) +
                        address +
                        getBlock(username) +
                        getBlock(status) +
                        getBlock(message);
                // 打印信息到日志
                sys_user_logger.info(s, args);
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
                    if (StringUtils.equalsAny(status, AppConstant.LOGIN_SUCCESS, AppConstant.LOGOUT, AppConstant.REGISTER)) {
                        draft.setStatus(AppConstant.SUCCESS);
                    } else if (AppConstant.LOGIN_FAIL.equals(status)) {
                        draft.setStatus(AppConstant.FAIL);
                    }
                });
                JSqlClient sqlClient = SpringUtils.getBean(JSqlClient.class);
                sqlClient.save(loginLog, SaveMode.INSERT_ONLY);
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
        String template = "[%s]";
        return Objects.isNull(msg) ?
                String.format(template, "") : String.format(template, msg);
    }
}
