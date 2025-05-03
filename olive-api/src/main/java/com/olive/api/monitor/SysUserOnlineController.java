package com.olive.api.monitor;

import com.olive.service.aop.log.Log;
import com.olive.model.LoginUser;
import com.olive.model.constant.CacheConstant;
import com.olive.model.constant.BusinessType;
import com.olive.model.record.SysUserOnline;
import com.olive.service.SysOnlineService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 在线用户监控
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/monitor/online")
public class SysUserOnlineController {
    private final SysOnlineService userOnlineService;
    private final CacheManager cacheManager;
    private final RedisTemplate redisTemplate;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String ipaddr, @RequestParam(required = false) String userName) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(CacheConstant.CACHE_LOGIN_TOKEN_KEY).build();
        Cursor<byte[]> cursor = redisTemplate.scan(scanOptions);
        ArrayList<String> keys = new ArrayList<>();
        while (cursor.hasNext()) {
            byte[] next = cursor.next();
            keys.add(new String(next));
        }

        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        Cache cache = cacheManager.getCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY);

        for (String key : keys) {
            LoginUser user = cache.get(key, LoginUser.class);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            } else if (StringUtils.isNotEmpty(userName) && Objects.nonNull(user.getUser())) {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));

        return Map.of(
                "code", HttpStatus.OK.value(),
                "msg", "查询成功",
                "rows", userOnlineList,
                "total", userOnlineList.size()
        );
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public void forceLogout(@PathVariable String tokenId) {
        cacheManager.getCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY).evict(tokenId);
    }
}
