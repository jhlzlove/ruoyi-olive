package com.olive.service;

import com.olive.model.SysNotice;
import com.olive.model.SysNoticeTable;
import com.olive.model.dto.SysNoticeSearch;
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
        return sqlClient.getEntities().save(notice).getTotalAffectedRowCount();
    }

    public int updateNotice(SysNotice notice) {
        return sqlClient.getEntities().save(notice).getTotalAffectedRowCount();
    }

    public void deleteNoticeByIds(List<Long> noticeIds) {
        sqlClient.deleteByIds(SysNotice.class, noticeIds);
    }
}
