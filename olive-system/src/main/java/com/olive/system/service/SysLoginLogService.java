package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.record.PageQuery;
import com.olive.framework.web.system.SysLoginLog;
import com.olive.framework.web.system.SysLoginLogTable;
import com.olive.framework.web.system.dto.SysLoginLogSearch;
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
public class SysLoginLogService {
    private final JSqlClient sqlClient;
    final SysLoginLogTable table = SysLoginLogTable.$;

    public Page<SysLoginLog> page(SysLoginLogSearch search, PageQuery page) {
        LocalDateTime beginDate = null;
        LocalDateTime endDate = null;
        if (page.beginTime() != null && page.endTime() != null) {
            beginDate = LocalDateUtil.dateStrToDateTime(page.beginTime());
            endDate = LocalDateUtil.dateStrToDateTime(page.beginTime());
        }

        return sqlClient.createQuery(table)
                .where(search)
                .where(table.loginTime().geIf(beginDate))
                .where(table.loginTime().leIf(endDate))
                .orderBy(table.loginTime().desc())
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public void cleanLoginLog() {
        sqlClient.createDelete(table).execute();
    }

    public int deleteLoginLogByIds(List<Long> infoIds) {
        return sqlClient.deleteByIds(SysLoginLog.class, infoIds).getTotalAffectedRowCount();
    }
}
