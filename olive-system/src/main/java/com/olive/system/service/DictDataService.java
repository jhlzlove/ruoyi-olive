package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.PageQuery;
import com.olive.system.domain.*;
import com.olive.system.domain.dto.SysDictDataSearch;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.DraftObjects;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class DictDataService {
    private final JSqlClient sqlClient;
    private final CacheService cacheService;
    private final SysDictDataTable table = SysDictDataTable.$;

    final SysDictDataFetcher simple_fetcher = Fetchers.SYS_DICT_DATA_FETCHER.allScalarFields();


    /**
     * 根据条件分页查询字典数据
     *
     * @param search 查询参数
     * @return 字典数据集合信息
     */
    public Page<SysDictData> selectDictDataList(SysDictDataSearch search, PageQuery pageRecord) {
        return sqlClient.createQuery(table)
                .where(search)
                .select(table.fetch(simple_fetcher))
                .fetchPage(pageRecord.pageNum() - 1, pageRecord.pageSize());
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    public String selectDictLabel(String dictType, String dictValue) {
        return sqlClient.createQuery(table)
                .where(table.dictType().eq(dictType))
                .where(table.dictValue().eq(dictValue))
                .select(table.dictLabel())
                .fetchOne();
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    public SysDictData selectDictDataById(Long dictCode) {
        return sqlClient.createQuery(table)
                .where(table.dictCode().eq(dictCode))
                .select(table.fetch(simple_fetcher))
                .fetchOne();
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictDataByIds(List<Long> dictCodes) {
        dictCodes.forEach(dictCode -> {
            SysDictData sysDictData = sqlClient.createQuery(table)
                    .where(table.dictCode().eq(dictCode))
                    .select(table.fetch(simple_fetcher))
                    .fetchOne();
            List<SysDictData> data = sqlClient.createQuery(table)
                    .where(table.dictType().eq(sysDictData.dictType()))
                    .select(table.fetch(simple_fetcher))
                    .execute();
            sqlClient.deleteById(SysDictData.class, dictCode);
            cacheService.put(CacheConstants.SYS_DICT_KEY + ":" + sysDictData.dictType(), data);
            // Cache cache = CacheUtils.getCache(CacheConstants.SYS_DICT_KEY);
            // cache.put(sysDictData.dictType(), data);
        });
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Transactional
    public int insertDictData(SysDictData data) {
        Long count = sqlClient.createQuery(table)
                .where(table.dictType().eq(data.dictType()))
                .where(table.dictValue().eq(data.dictValue()))
                .select(table.count())
                .fetchOne();
        if (count > 0) {
            throw new ServiceException("键值不能重复！");
        }
        int row = sqlClient.save(data).getTotalAffectedRowCount();
        if (row > 0) {
            List<SysDictData> result = list(data.dictType());
            cacheService.put(CacheConstants.SYS_DICT_KEY + ":" + data.dictType(), result);
            // Cache cache = CacheUtils.getCache(CacheConstants.SYS_DICT_KEY);
            // cache.put(data.dictType(), result);
        }
        return row;
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Transactional
    public int updateDictData(SysDictData data) {
        Long count = sqlClient.createQuery(table)
                .where(table.dictCode().ne(data.dictCode()))
                .where(table.dictType().eq(data.dictType()))
                .where(table.dictValue().eq(data.dictValue()))
                .select(table.count())
                .fetchOne();
        if (count > 0) {
            throw new ServiceException("键值不能重复！");
        }
        SysDictData produce = SysDictDataDraft.$.produce(data, draft -> {
            DraftObjects.set(draft, SysDictDataProps.UPDATE_TIME, LocalDateUtil.dateTime());
        });
        int row = sqlClient.save(produce).getTotalAffectedRowCount();
        if (row > 0) {
            List<SysDictData> dictDatas = list(data.dictType());
            cacheService.put(CacheConstants.SYS_DICT_KEY + ":" + data.dictType(), dictDatas);
            // Cache cache = CacheUtils.getCache(CacheConstants.SYS_DICT_KEY);
            // cache.put(data.dictType(), dictDatas);
        }
        return row;
    }

    private List<SysDictData> list(String dictType) {
        return sqlClient.createQuery(table)
                .where(table.dictType().eq(dictType))
                .select(table.fetch(Fetchers.SYS_DICT_DATA_FETCHER.allScalarFields()))
                .execute();
    }

    public List<SysDictData> selectDictDataByType(String dictType) {
        return sqlClient.createQuery(table)
                .where(table.dictType().eq(dictType))
                .select(table)
                .execute();
    }
}
