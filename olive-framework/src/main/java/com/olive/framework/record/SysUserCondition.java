package com.olive.framework.record;

import java.util.List;

public record SysUserCondition(
            Long deptId,
            String userName,
            String phonenumber,
            List<Long> roleIds
    ){}