package com.olive.framework.cache;

public interface CacheNoTimeOut {

    public <T> void setCacheObject(final String cacheName,final String key, final T value);

}
