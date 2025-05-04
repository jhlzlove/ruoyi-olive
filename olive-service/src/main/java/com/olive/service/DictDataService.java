package com.olive.service;

import com.olive.model.SysDictData;
import com.olive.model.SysDictDataFetcher;
import com.olive.model.SysDictDataTable;
import com.olive.model.constant.CacheConstant;
import com.olive.model.dto.SysDictDataSearch;
import com.olive.model.exception.SysDictException;
import com.olive.model.record.PageQuery;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
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
    private static final SysDictDataTable table = SysDictDataTable.$;

    /**
     * 根据条件分页查询字典数据
     *
     * @param search 查询参数
     * @return 字典数据集合信息
     */
    public Page<SysDictData> page(SysDictDataSearch search, PageQuery pageRecord) {
        return sqlClient.createQuery(table)
                .where(search)
                .select(table.fetch(SysDictDataFetcher.$.allScalarFields()))
                .fetchPage(pageRecord.pageNum() - 1, pageRecord.pageSize());
    }

    /**
     * 根据 dictCode 修改字典项
     * @param dictCode  字典项主键
     * @return 结果
     */
    public SysDictData getByDictCode(long dictCode) {
        return sqlClient.createQuery(table)
                .where(table.dictCode().eq(dictCode))
                .select(table.fetch(SysDictDataFetcher.$.allScalarFields()))
                .fetchOne();
    }

    /**
     * 根据 dictType 查询 data 列表
     *
     * @param dictType dictType
     * @return SysDictData
     */
    public List<SysDictData> listByDictType(String dictType) {
        return sqlClient.createQuery(table)
                .where(table.dictType().eq(dictType))
                .select(table.fetch(SysDictDataFetcher.$.allScalarFields()))
                .execute();
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Transactional
    public boolean add(SysDictData data) {
        boolean exists = sqlClient.createQuery(table)
                .where(table.dictType().eq(data.dictType()))
                .where(table.dictValue().eq(data.dictValue()))
                .select(table.count())
                .exists();
        if (exists) {
            throw SysDictException.dictDataExist(data.dictType(), data.dictLabel(), "键值不能重复！");
        }
        return sqlClient.getEntities().save(data).getTotalAffectedRowCount() > 0;
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Transactional
    public boolean update(SysDictData data) {
        Long count = sqlClient.createQuery(table)
                .where(table.dictCode().ne(data.dictCode()))
                .where(table.dictType().eq(data.dictType()))
                .where(table.dictValue().eq(data.dictValue()))
                .select(table.count())
                .fetchOne();
        if (count > 0) {
            throw SysDictException.dictDataExist(
                    data.dictType(),
                    data.dictLabel(),
                    data.dictValue(),
                    "键值不能重复！"
            );
        }
        return sqlClient.getEntities().save(data).getTotalAffectedRowCount() > 0;
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> dictCodes) {
        dictCodes.forEach(dictCode -> {
            SysDictData sysDictData = sqlClient.createQuery(table)
                    .where(table.dictCode().eq(dictCode))
                    .select(table.fetch(SysDictDataFetcher.$.allScalarFields()))
                    .fetchOne();
            List<SysDictData> data = sqlClient.createQuery(table)
                    .where(table.dictType().eq(sysDictData.dictType()))
                    .select(table.fetch(SysDictDataFetcher.$.allScalarFields()))
                    .execute();
            sqlClient.deleteById(SysDictData.class, dictCode);
        });
    }
}
