package com.olive.system.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 缓存信息
 *
 * @author ruoyi
 */
@Setter
@Getter
public class SysCache {
    /**
     * 缓存名称
     */
    private String cacheName = "";

    /**
     * 缓存键名
     */
    private String cacheKey = "";

    /**
     * 缓存内容
     */
    private String cacheValue = "";

    /**
     * 备注
     */
    private String remark = "";

    public SysCache() {
    }

    public SysCache(String cacheName, String remark) {
        this.cacheName = cacheName;
        this.remark = remark;
    }

}
