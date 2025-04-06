package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;
import org.babyfish.jimmer.error.ErrorField;

/**
 * 部门相关的异常
 *
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SysDeptError {

    NOT_PERMISSION,

    HAS_USER_NOT_ALLOW_DELETE,

    HAS_CHILD_DEPT_NOT_ALLOW_DELETE,

    @ErrorField(name = "deptName", type = String.class)
    DEPT_NAME_EXIST,

    HAS_NORMAL_CHILD_DEPT,
    ;
}
