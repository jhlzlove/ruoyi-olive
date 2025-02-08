package com.olive.web.controller.tool;

import com.olive.common.response.R;
import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.record.PageQuery;
import com.olive.framework.web.system.SysUser;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * swagger 用户测试方法
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {
    private final JSqlClient sqlClient;

    @GetMapping("/users")
    public Page<SysUser> getUsers(PageQuery pageQuery) {
        return null;
    }

    @GetMapping("/localtime")
    public R test() {
        return R.ok(LocalDateUtil.dateTime());
    }

    @GetMapping("/ok")
    public R ok() {
        return R.ok();
    }
}
