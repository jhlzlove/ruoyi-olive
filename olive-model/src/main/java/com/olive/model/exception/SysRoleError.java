package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysRoleError {

    @ErrorField(name = "roleName", type = String.class)
    ROLE_NAME_EXIST,

    @ErrorField(name = "roleKey", type = String.class)
    ROLE_KEY_EXIST,
    ;
}
