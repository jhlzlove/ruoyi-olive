package com.olive.model.record;

import java.util.List;

public record SysUserCondition(
            Long deptId,
            String userName,
            String phonenumber,
            Long roleId,
            List<Long> roleIds
    ){}