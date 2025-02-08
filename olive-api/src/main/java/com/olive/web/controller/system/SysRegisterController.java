package com.olive.web.controller.system;

import com.olive.common.response.R;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.RegisterBody;
import com.olive.framework.util.StringUtils;
import com.olive.system.service.SysConfigService;
import com.olive.system.service.SysRegisterService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
public class SysRegisterController {
    private final SysRegisterService registerService;
    private final SysConfigService configService;

    @PostMapping("/register")
    public R register(@RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            throw new ServiceException("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? R.ok() : R.error(msg);
    }
}
