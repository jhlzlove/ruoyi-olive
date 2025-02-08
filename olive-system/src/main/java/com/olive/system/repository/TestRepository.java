package com.olive.system.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;

/**
 * @author jhlz
 * @version x.x.x
 */
public class TestRepository extends AbstractJavaRepository {
    public TestRepository(JSqlClient sql) {
        super(sql);
    }
}
