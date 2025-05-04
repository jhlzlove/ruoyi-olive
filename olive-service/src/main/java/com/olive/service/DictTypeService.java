package com.olive.service;

import com.olive.model.*;
import com.olive.model.constant.CacheConstant;
import com.olive.model.exception.SysDictException;
import com.olive.model.record.PageQuery;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class DictTypeService {

    private static final Logger log = LoggerFactory.getLogger(DictTypeService.class);
    private final JSqlClient sqlClient;
    private final SysDictTypeTable table = SysDictTypeTable.$;
    private final SysDictDataTable dataTable = SysDictDataTable.$;
    private final DictDataService dictDataService;
    private final CacheManager cacheManager;

    public Page<SysDictType> page(SysDictType dictType, PageQuery page) {
        return sqlClient.createQuery(table)
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public List<SysDictType> list() {
        return sqlClient.getEntities().findAll(SysDictType.class);
    }

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    public SysDictType info(Long dictId) {
        return sqlClient.createQuery(table)
                .where(table.dictId().eq(dictId))
                .select(table)
                .fetchOne();
    }

    /**
     * 项目启动时加载到缓存中
     */
    @PostConstruct
    public void init() {
        loadingDictCache();
    }

    /**
     * 加载字典缓存数据
     */
    public void loadingDictCache() {
        List<SysDictData> result = sqlClient.createQuery(dataTable)
                .where(dataTable.status().eq("0"))
                .select(dataTable.fetch(SysDictDataFetcher.$.allScalarFields()))
                .execute();

        Map<String, List<SysDictData>> dictDataMap = result.stream()
                .collect(Collectors.groupingBy(SysDictData::dictType));
    }

    /**
     * 清空字典缓存数据
     */
    public void clearDictCache() {
        cacheManager.getCache(CacheConstant.CACHE_DICT_KEY).clear();
    }

    /**
     * 重置字典缓存数据
     */
    public void resetDictCache() {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 新增保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Transactional
    public boolean add(SysDictType dict) {
        return sqlClient.getEntities().save(dict).getTotalAffectedRowCount() > 0;
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Transactional
    public int update(SysDictType dict) {
        int row = sqlClient.getEntities().save(dict).getTotalAffectedRowCount();
        return row;
    }

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     */
    public void delete(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            SysDictType dictType = info(dictId);
            Long row = sqlClient.createQuery(dataTable)
                    .where(dataTable.dictType().eq(dictType.dictType()))
                    .select(table.count())
                    .fetchOne();
            if (row > 0) {
                throw SysDictException.dictTypeUsed("字典类型已分配,不能删除", dictType.dictName());
            }
            sqlClient.deleteById(SysDictType.class, dictId);
        }
    }

    public List<SysDictData> listByDictType(String dictType) {
        SysDictDataTable dataTable = SysDictDataTable.$;
        return sqlClient.createQuery(dataTable)
                .where(dataTable.dictType().eq(dictType))
                .select(dataTable.fetch(SysDictDataFetcher.$.allScalarFields()))
                .execute();
    }
}
