package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.PageQuery;
import com.olive.framework.util.Convert;
import com.olive.framework.util.StringUtils;
import com.olive.system.domain.SysConfig;
import com.olive.system.domain.SysConfigTable;
import com.olive.system.domain.dto.SysConfigSearch;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final CacheService cacheService;
    final SysConfigTable table = SysConfigTable.$;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    // @DataSource(DataSourceType.MASTER)
    public SysConfig selectConfigById(Long configId) {
        return sqlClient.findById(SysConfig.class, configId);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    public String selectConfigByKey(String configKey) {

        String configValue = cacheService.get(CacheConstants.SYS_CONFIG_KEY, configKey);
        // String configValue = Convert.toStr(getCache().get(configKey, String.class));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        SysConfig sysConfig = sqlClient.createQuery(table)
                .where(table.configKey().eqIf(configKey))
                .select(table)
                .fetchOneOrNull();
        if (Objects.nonNull(sysConfig)) {
            cacheService.put(CacheConstants.SYS_CONFIG_KEY, configKey, sysConfig.configValue());
            return sysConfig.configValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     *
     * @return true开启，false关闭
     */
    public boolean selectCaptchaEnabled() {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        return Convert.toBool(captchaEnabled, true);
    }

    /**
     * 查询参数配置列表
     *
     * @param search 参数配置信息
     * @return 参数配置集合
     */
    public Page<SysConfig> page(SysConfigSearch search, PageQuery page) {
        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;
        if (page.beginTime() != null && page.endTime() != null) {
            beginTime = LocalDateUtil.dateStrToDateTime(page.beginTime());
            endTime = LocalDateUtil.dateStrToDateTime(page.endTime());
        }
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.createTime().geIf(beginTime))
                .where(table.createTime().leIf(endTime))
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
        // return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public int insertConfig(SysConfig config) {
        int row = sqlClient.save(config).getTotalAffectedRowCount();
        if (row > 0) {
            cacheService.put(CacheConstants.SYS_CONFIG_KEY, config.configKey(), config.configValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public int updateConfig(SysConfig config) {
        int row = sqlClient.update(config).getTotalAffectedRowCount();
        cacheService.remove(CacheConstants.SYS_CONFIG_KEY, config.configKey());
        if (row > 0) {
            cacheService.put(CacheConstants.SYS_CONFIG_KEY, config.configKey(), config.configValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    public boolean deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.configType())) {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.configKey()));
            }
            sqlClient.deleteById(SysConfig.class, configId);
            cacheService.remove(CacheConstants.SYS_CONFIG_KEY, config.configKey());
        }
        return true;
    }

    /**
     * 加载参数缓存数据
     */
    public void loadingConfigCache() {
        List<SysConfig> configsList = sqlClient.createQuery(table)
                .select(table)
                .execute();
        for (SysConfig config : configsList) {
            cacheService.put(CacheConstants.SYS_CONFIG_KEY, config.configKey(), config.configValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    public void clearConfigCache() {
        cacheService.clear(CacheConstants.SYS_CONFIG_KEY);
    }

    /**
     * 重置参数缓存数据
     */
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(SysConfig config) {
        if (checkConfigKeyUnique(config)) {
            throw new ServiceException("新增参数'" + config.configName() + "'失败，参数键名已存在");
        }
        return sqlClient.save(config).getTotalAffectedRowCount() > 0;
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
                .select(table)
                .fetchOneOrNull() != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean edit(SysConfig config) {
        if (checkConfigKeyUnique(config)) {
            throw new ServiceException("修改参数'" + config.configName() + "'失败，参数键名已存在");
        }
        return sqlClient.save(config).getTotalAffectedRowCount() > 0;
    }
}
