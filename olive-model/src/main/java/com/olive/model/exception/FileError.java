package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;

/**
 * 文件异常簇
 * @author jhlz
 * @version x.x.x
 */
@ErrorFamily
public enum FileError {

    /**
     * 文件名过长
     */
    FILE_NAME_TOO_LONG,
    /**
     * 文件大小超出限制
     */
    FILE_SIZE_EXCEEDS_LIMIT,
    /**
     * 文件数量超出限制
     */
    FILE_COUNT_EXCEEDS_LIMIT,

    ;
}
