package com.olive.web.controller.monitor;

import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.enums.BusinessType;
import com.olive.framework.log.Log;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.LoginUser;
import com.olive.system.domain.SysUserOnline;
import com.olive.system.service.SysOnlineService;
import lombok.AllArgsConstructor;
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
    private final CacheService cacheService;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String ipaddr, @RequestParam(required = false) String userName) {
        // Collection<String> keys = CacheUtils.getkeys(CacheConstants.LOGIN_TOKEN_KEY);
        Collection<String> keys = cacheService.keys(CacheConstants.LOGIN_TOKEN_KEY);
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys) {
            LoginUser user = cacheService.get(CacheConstants.LOGIN_TOKEN_KEY + ":" + key, LoginUser.class);
            // LoginUser user = CacheUtils.get(CacheConstants.LOGIN_TOKEN_KEY, key, LoginUser.class);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
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
        cacheService.remove(CacheConstants.LOGIN_TOKEN_KEY, tokenId);
    }
}
