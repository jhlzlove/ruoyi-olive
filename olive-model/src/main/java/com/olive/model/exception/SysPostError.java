package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysPostError {

    @ErrorField(name = "postName", type = String.class)
    POST_NAME_EXIST,

    @ErrorField(name = "postCode", type = String.class)
    POST_CODE_EXIST,
    ;
}
