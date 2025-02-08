package com.olive.common.response;

import jakarta.annotation.Nullable;

/**
 * 一般响应体
 * 只推荐失败时使用，成功时直接返回结果
 *
 * @author jhlz
 * @version x.x.x
 */
public record R(
        /* 响应码 */
        int code,
        /* 简要信息 */
        @Nullable
        String msg,
        /* 详细信息 */
        @Nullable
        String detail,
        /* 数据 */
        @Nullable
        Object data
) {

    public static R ok() {
        return ok(null);
    }

    public static R ok(Object data) {
        return ok(200, "操作成功", null, data);
    }

    public static R error() {
        return error("操作失败");
    }

    public static R error(String msg) {
        return error(500, msg, null);
    }

    public static R error(int code, String msg) {
        return error(code, msg, null);
    }

    public static R error(int code, String msg, String detail) {
        return ok(code, msg, detail, null);
    }

    /**
     * 原始方法
     *
     * @param code   状态码
     * @param msg    简要信息，可以为 null
     * @param detail 详细信息，可以为 null
     * @param data   数据，可以为 null
     * @return R 结构体
     */
    public static R ok(int code, String msg, String detail, Object data) {
        return new R(code, msg, detail, data);
    }
}
