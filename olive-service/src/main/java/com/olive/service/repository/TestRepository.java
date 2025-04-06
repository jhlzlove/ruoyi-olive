package com.olive.service.repository;

import com.olive.model.SysUser;
import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;

/**
 * @author jhlz
 * @version x.x.x
 */
public class TestRepository extends AbstractJavaRepository<SysUser, Long> {
    public TestRepository(JSqlClient sql) {
        super(sql);
    }

}
