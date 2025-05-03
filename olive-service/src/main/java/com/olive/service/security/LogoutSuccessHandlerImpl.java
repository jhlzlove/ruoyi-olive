package com.olive.service.security;

import com.olive.base.response.R;
import com.olive.model.LoginUser;
import com.olive.model.constant.AppConstant;
import com.olive.service.manager.AsyncManager;
import com.olive.service.manager.factory.AsyncFactory;
import com.olive.service.TokenService;
import com.olive.service.util.JSON;
import com.olive.service.util.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author ruoyi
 */
@Configuration
@AllArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    private final TokenService tokenService;
    private final MessageSource messageSource;

    /**
     * 退出处理
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (Objects.nonNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, AppConstant.LOGOUT,
                    MessageUtils.message("user.logout.success")));
        }
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().print(JSON.toJSON(R.ok(messageSource.getMessage("user.logout.success", null, LocaleContextHolder.getLocale()))));
    }
}
