package com.olive.model.record;

/**
 * @author jhlz
 * @version x.x.x
 */
public record SysCache(
        /**
         * 缓存名称
         */
        String cacheName,

        /**
         * 缓存键名
         */
        String cacheKey,

        /**
         * 缓存内容
         */
        String cacheValue,

        /**
         * 备注
         */
        String remark
) {

    public SysCache(String cacheName, String remark) {
        this(cacheName, "", "", remark);
    }

    public SysCache(String cacheName, String cacheKey, String cacheValue) {
        this(cacheName, cacheKey, cacheValue, "");
    }

}
