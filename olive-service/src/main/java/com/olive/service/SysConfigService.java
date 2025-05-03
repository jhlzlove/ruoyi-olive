package com.olive.service;

import com.olive.model.SysConfig;
import com.olive.model.SysConfigFetcher;
import com.olive.model.SysConfigTable;
import com.olive.model.constant.AppConstant;
import com.olive.model.constant.CacheConstant;
import com.olive.model.dto.SysConfigSearch;
import com.olive.model.exception.SysConfigException;
import com.olive.model.record.PageQuery;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysConfigService {
    private final JSqlClient sqlClient;
    private final CacheManager cacheManager;
    final SysConfigTable table = SysConfigTable.$;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        loadingConfigCache();
    }

    /**
     * 查询参数配置列表
     *
     * @param search 参数配置信息
     * @return 参数配置集合
     */
    public Page<SysConfig> page(SysConfigSearch search, PageQuery pageQuery) {
        PageQuery page = PageQuery.create(pageQuery);
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.createTime().geIf(page.beginTime()))
                .where(table.createTime().leIf(page.beginTime()))
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    public String listByConfigKey(String configKey) {
        Cache cache = cacheManager.getCache(CacheConstant.CACHE_CONFIG_KEY);
        String configValue = cache.get(configKey, String.class);
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        String value = sqlClient.createQuery(table)
                .where(table.configKey().eqIf(configKey))
                .select(table.configValue())
                .fetchOneOrNull();
        if (Objects.nonNull(value)) {
            cache.put(configKey, value);
            return value;
        }
        return null;
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    public SysConfig info(Long configId) {
        return sqlClient.findById(SysConfig.class, configId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(SysConfig config) {
        if (checkConfigKeyUnique(config)) {
            throw SysConfigException.configNameExist("新增参数名称已存在", config.configName());
        }
        return sqlClient.getEntities().save(config).getTotalAffectedRowCount() > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysConfig config) {
        if (checkConfigKeyUnique(config)) {
            throw SysConfigException.configNameExist("修改参数名称已存在", config.configName());
        }
        return sqlClient.getEntities().save(config).getTotalAffectedRowCount() > 0;
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    public boolean delete(List<Long> configIds) {
        List<SysConfig> list = sqlClient.createQuery(table)
                .where(table.configType().eq(AppConstant.Y))
                .where(table.configId().in(configIds))
                .select(table.fetch(
                        SysConfigFetcher.$
                                .configType()
                                .configKey()
                ))
                .execute();

        Cache cache = cacheManager.getCache(CacheConstant.CACHE_CONFIG_KEY);
        list.forEach(it -> {
            if (Objects.equals(AppConstant.Y, it.configType())) {
                throw SysConfigException.noAllowDelete("内置参数无法删除", it.configKey());
            }
            sqlClient.deleteById(SysConfig.class, it.configId());
            cache.evict(it.configKey());
        });
        return true;
    }

    /**
     * 获取验证码开关
     *
     * @return true开启，false关闭
     */
    public boolean selectCaptchaEnabled() {
        String captchaEnabled = listByConfigKey(CacheConstant.CACHE_CAPTCHA_KEY);
        return switch (captchaEnabled) {
            case "true", "yes", "ok", "1" -> true;
            case "false", "no", "0" -> false;
            default -> true;
        };
    }

    /**
     * 加载参数缓存数据
     */
    public void loadingConfigCache() {
        List<SysConfig> all = sqlClient.getEntities().findAll(SysConfig.class);
        Cache cache = cacheManager.getCache(CacheConstant.CACHE_CONFIG_KEY);
        all.forEach(it -> cache.put(it.configKey(), it.configValue()));
    }

    /**
     * 清空参数缓存数据
     */
    public void clearConfigCache() {
        cacheManager.getCache(CacheConstant.CACHE_CONFIG_KEY).clear();
    }

    /**
     * 重置参数缓存数据
     */
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    private boolean checkConfigKeyUnique(SysConfig config) {
        return sqlClient.createQuery(table)
                .where(table.configId().ne(config.configId()))
                .where(table.configName().eq(config.configName()))
                .exists();
    }
}
