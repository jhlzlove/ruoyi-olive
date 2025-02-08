package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.record.PageQuery;
import com.olive.framework.web.system.SysOperLog;
import com.olive.framework.web.system.SysOperLogTable;
import com.olive.framework.web.system.dto.SysOperLogSearch;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Page<SysOperLog> page(SysOperLogSearch search, PageQuery page) {
        LocalDateTime beginDate = null;
        LocalDateTime endDate = null;
        if (page.beginTime() != null && page.endTime() != null) {
            beginDate = LocalDateUtil.dateStrToDateTime(page.beginTime());
            endDate = LocalDateUtil.dateStrToDateTime(page.beginTime());
        }
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.operTime().geIf(beginDate))
                .where(table.operTime().leIf(endDate))
                .orderBy(table.operTime().desc())
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public int deleteOperLogByIds(List<Long> operIds) {
        return sqlClient.deleteByIds(SysOperLog.class, operIds).getTotalAffectedRowCount();
    }

    public void cleanOperLog() {
        sqlClient.createDelete(table).execute();
    }
}
