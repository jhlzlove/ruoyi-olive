package com.olive.service;

import com.olive.model.SysLoginLog;
import com.olive.model.SysLoginLogTable;
import com.olive.model.dto.SysLoginLogSearch;
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
public class SysLoginLogService {
    private final JSqlClient sqlClient;
    final SysLoginLogTable table = SysLoginLogTable.$;

    public Page<SysLoginLog> page(SysLoginLogSearch search, PageQuery pageQuery) {
        PageQuery page = PageQuery.create(pageQuery);

        return sqlClient.createQuery(table)
                .where(search)
                .where(table.loginTime().geIf(page.beginTime()))
                .where(table.loginTime().leIf(page.endTime()))
                .orderBy(table.loginTime().desc())
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public boolean cleanLoginLog() {
        return sqlClient.createDelete(table).execute() > 0;
    }

    public boolean delete(List<Long> infoIds) {
        return sqlClient.deleteByIds(SysLoginLog.class, infoIds).getTotalAffectedRowCount() > 0;
    }
}
