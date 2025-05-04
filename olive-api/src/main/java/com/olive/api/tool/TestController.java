package com.olive.api.tool;

import com.olive.base.response.R;
import com.olive.base.util.LocalDateUtil;
import com.olive.model.SysUser;
import com.olive.model.record.PageQuery;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    private final CacheManager cacheManager;

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

    @GetMapping("/es")
    public R es() {
        // new RestClientHttpClient()
        // RestClient.builder(HttpHost.create(""));
        boolean ping = false;
        return R.ok(ping);
    }

    @GetMapping("/cache")
    public String getCache(String prefix, String key) {
        String test = cacheManager.getCache(prefix).get(key, String.class);
        if (Objects.nonNull(test)) {
            return test;
        }
        return "value is  null";
    }

    @PostMapping("/cache")
    public R cache(String prefix) {
        cacheManager.getCache(prefix).put("111", "Apple");
        return R.ok();
    }


    @GetMapping("/http")
    public String springHttpTest() {
        // DatabaseMetaData
        return "";
    }
}
