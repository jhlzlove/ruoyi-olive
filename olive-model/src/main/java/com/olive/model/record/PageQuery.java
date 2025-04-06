package com.olive.model.record;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 查询参数
 *
 * @author jhlz
 * @version x.x.x
 */
public record PageQuery(
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) {
    public PageQuery(int pageNum, int pageSize) {
        this(pageNum, pageSize, null, null);
    }

    /**
     * 默认的查询从第一页开始，每页 10 条数据
     *
     * @param pageQuery 查询参数
     * @return PageQuery
     */
    public static PageQuery create(PageQuery pageQuery) {
        return Objects.nonNull(pageQuery) ?
                pageQuery : new PageQuery(1, 10, null, null);
    }
}
