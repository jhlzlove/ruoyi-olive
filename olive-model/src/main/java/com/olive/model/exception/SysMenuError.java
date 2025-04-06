package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysMenuError {

    @ErrorField(name = "menuName", type = String.class)
    MENU_NAME_EXIST,

    @ErrorField(name = "menuPath", type = String.class)
    MENU_PATH_NOT_ALLOW,

    PARENT_MENU_NOT_SELECT_SELF,

    HAS_CHILD_MENU,

    MENU_USED,
    ;
}
