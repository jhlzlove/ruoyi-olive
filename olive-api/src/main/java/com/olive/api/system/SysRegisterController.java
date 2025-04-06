package com.olive.api.system;

import com.olive.base.response.R;
import com.olive.model.record.RegisterBody;
import com.olive.service.SysConfigService;
import com.olive.service.SysRegisterService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        if (!("true".equals(configService.listByConfigKey("sys.account.registerUser")))) {
            throw new RuntimeException("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? R.ok() : R.error(msg);
    }
}
