package com.olive.system.service;

import com.olive.framework.record.PageQuery;
import com.olive.system.domain.SysNotice;
import com.olive.system.domain.SysNoticeTable;
import com.olive.system.domain.dto.SysNoticeSearch;
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
public class SysNoticeService {
    private final JSqlClient sqlClient;
    final SysNoticeTable table = SysNoticeTable.$;

    public Page<SysNotice> page(SysNoticeSearch search, PageQuery page) {
        return sqlClient.createQuery(table)
                .where(search)
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public SysNotice selectNoticeById(long noticeId) {
        return sqlClient.findById(SysNotice.class, noticeId);
    }

    public int insertNotice(SysNotice notice) {
        return sqlClient.save(notice).getTotalAffectedRowCount();
    }

    public int updateNotice(SysNotice notice) {
        return sqlClient.update(notice).getTotalAffectedRowCount();
    }

    public void deleteNoticeByIds(List<Long> noticeIds) {
        sqlClient.deleteByIds(SysNotice.class, noticeIds);
    }
}
