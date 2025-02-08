package com.olive.framework.record;

/**
 * 分页参数
 * @author jhlz
 * @version x.x.x
 */
public record PageQuery(
        // @DefaultValue("1")
        int pageNum,
        // @DefaultValue("10")
        int pageSize,

        String beginTime,
        String endTime
) {
        public PageQuery(int pageNum, int pageSize) {
                this(pageNum, pageSize, null, null);
        }

}
