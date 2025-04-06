package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * 用户相关的异常
 * @author jhlz
 * @version 0.0.1
 */

@ErrorFamily
public enum SysUserError {


    @ErrorField(name = "username", type = String.class)
    USERNAME_EXIST,
    @ErrorField(name = "phone", type = String.class)
    PHONE_EXIST,
    @ErrorField(name = "email", type = String.class)
    EMAIL_EXIST,

    ;
}
