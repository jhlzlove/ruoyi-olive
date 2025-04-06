package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysConfigError {
    @ErrorField(name = "configKey", type = String.class)
    NO_ALLOW_DELETE,

    @ErrorField(name = "configName", type = String.class)
    CONFIG_NAME_EXIST,
    ;
}
