package com.olive.service;

import com.olive.model.SysOperLog;
import com.olive.model.SysOperLogTable;
import com.olive.model.dto.SysOperLogSearch;
import com.olive.model.record.PageQuery;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysOperLogService {
    private final JSqlClient sqlClient;
    final SysOperLogTable table = SysOperLogTable.$;

    public Page<SysOperLog> page(SysOperLogSearch search, PageQuery pageQuery) {
        PageQuery page = PageQuery.create(pageQuery);

        return sqlClient.createQuery(table)
                .where(search)
                .where(table.operTime().geIf(page.beginTime()))
                .where(table.operTime().leIf(page.endTime()))
                .orderBy(table.operTime().desc())
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public boolean delete(List<Long> operIds) {
        return sqlClient.deleteByIds(SysOperLog.class, operIds).getTotalAffectedRowCount() > 0;
    }

    public void cleanOperLog() {
        sqlClient.createDelete(table).execute();
    }
}
