package com.olive.system.service;

import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.PageQuery;
import com.olive.system.domain.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

;

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
    private final CacheService cacheService;

    public Page<SysDictType> selectDictTypeList(SysDictType dictType, PageQuery page) {
        return sqlClient.createQuery(table)
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public List<SysDictType> selectDictTypeAll() {
        return sqlClient.getEntities().findAll(SysDictType.class);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectDictDataByType(String dictType) {
        List<SysDictData> dictDatas = (List<SysDictData>) cacheService.get(CacheConstants.SYS_DICT_KEY + ":" + dictType, List.class);
        // Cache cache = CacheUtils.getCache(CacheConstants.SYS_DICT_KEY);
        // List<SysDictData> dictDatas = (List<SysDictData>) cache.get(dictType, List.class);
        if (CollectionUtils.isNotEmpty(dictDatas)) {
            return dictDatas;
        }
        dictDatas = sqlClient.createQuery(dataTable)
                .where(dataTable.dictType().eq(dictType))
                .select(dataTable.fetch(Fetchers.SYS_DICT_DATA_FETCHER.allScalarFields()))
                .execute();
        if (CollectionUtils.isNotEmpty(dictDatas)) {
            cacheService.put(CacheConstants.SYS_DICT_KEY, dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    public SysDictType selectDictTypeById(Long dictId) {
        return sqlClient.createQuery(table)
                .where(table.dictId().eq(dictId))
                .select(table)
                .fetchOne();
    }

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    public SysDictType selectDictTypeByType(String dictType) {
        return sqlClient.createQuery(table)
                .where(table.dictType().eq(dictType))
                .select(table)
                .fetchOne();
    }

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     */
    public void deleteDictTypeByIds(Long[] dictIds) {

        for (Long dictId : dictIds) {
            SysDictType dictType = selectDictTypeById(dictId);
            Long row = sqlClient.createQuery(dataTable)
                    .where(dataTable.dictType().eq(dictType.dictType()))
                    .select(table.count())
                    .fetchOne();
            if (row > 0) {
                throw new ServiceException("%1$s已分配,不能删除".formatted(dictType.dictName()));
            }
            sqlClient.deleteById(SysDictType.class, dictId);
            cacheService.remove(CacheConstants.SYS_DICT_KEY, dictType.dictType());
            // CacheUtils.getCache(CacheConstants.SYS_DICT_KEY).evict(dictType.dictType());
        }
    }

    /**
     * 加载字典缓存数据
     */
    public void loadingDictCache() {
        List<SysDictData> result = sqlClient.createQuery(dataTable)
                .where(dataTable.status().eq("0"))
                .select(dataTable                )
                .execute();

        Map<String, List<SysDictData>> dictDataMap = result.stream().collect(Collectors.groupingBy(SysDictData::dictType));

        // Map<String, List<SysDictData>> dictDataMap = dictDataMapper.selectDictDataList(dictData).stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        for (Map.Entry<String, List<SysDictData>> entry : dictDataMap.entrySet()) {
            cacheService.put(CacheConstants.SYS_DICT_KEY + ":" + entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(SysDictData::dictSort)).collect(Collectors.toList()));
            // CacheUtils.getCache(CacheConstants.SYS_DICT_KEY).put(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(SysDictData::dictSort)).collect(Collectors.toList()));
        }
    }

    /**
     * 清空字典缓存数据
     */
    public void clearDictCache() {
        cacheService.clear(CacheConstants.SYS_DICT_KEY);
        // CacheUtils.getCache(CacheConstants.SYS_DICT_KEY).clear();
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
    public int insertDictType(SysDictType dict) {
        return sqlClient.insert(dict).getTotalAffectedRowCount();
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Transactional
    public int updateDictType(SysDictType dict) {
        int row = sqlClient.update(dict).getTotalAffectedRowCount();
        if (row > 0) {
            List<SysDictData> dictDatas = dictDataService.selectDictDataByType(dict.dictType());
            cacheService.put(CacheConstants.SYS_DICT_KEY + ":" + dict.dictType(), dictDatas);
            // CacheUtils.getCache(CacheConstants.SYS_DICT_KEY).put(dict.dictType(), dictDatas);
        }
        return row;
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    public boolean checkDictTypeUnique(SysDictType dict) {
        Long dictId = Objects.isNull(dict.dictId()) ? -1L : dict.dictId();
        SysDictType dictType = sqlClient.createQuery(table)
                .where(table.dictType().eq(dict.dictType()))
                .select(table)
                .fetchOne();
        // com.olive.framework.web.system.SysDictType dictType = dictTypeMapper.checkDictTypeUnique(dict.getDictType());
        if (Objects.nonNull(dictType) && dictType.dictId() != dictId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
