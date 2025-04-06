package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;
import org.babyfish.jimmer.error.ErrorFields;

/**
 * 字典相关的异常
 *
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysDictError {
    /**
     * dict type
     */
    @ErrorFields(value = {
            @ErrorField(name = "dictType", type = String.class),
            @ErrorField(name = "dictName", type = String.class)
    })
    DICT_TYPE_EXIST,

    @ErrorField(name = "dictName", type = String.class)
    DICT_TYPE_USED,

    /**
     * dict data
     */
    @ErrorFields(value = {
            @ErrorField(name = "dictType", type = String.class),
            @ErrorField(name = "dictLabel", type = String.class),
            @ErrorField(name = "dictValue", type = String.class)
    })
    DICT_DATA_EXIST,

    ;
}
