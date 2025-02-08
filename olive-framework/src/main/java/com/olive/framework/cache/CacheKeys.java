package com.olive.framework.cache;

import org.springframework.cache.Cache;

import java.util.Set;

public interface CacheKeys {

    public Set<String> getCachekeys(final Cache cache);
}
