package com.olive.test;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.runtime.ConnectionManager;
import org.babyfish.jimmer.sql.runtime.DatabaseValidationMode;
import org.babyfish.jimmer.sql.runtime.Executor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class AssociationSaveTest {
    private static final Logger log = LoggerFactory.getLogger(AssociationSaveTest.class);
    static JSqlClient sqlClient;

    @BeforeAll
    public static void init() throws SQLException {

        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://172.31.50.254:3308/test",
                "root",
                "root"
        );

        sqlClient = JSqlClient.newBuilder()
                .setConnectionManager(ConnectionManager.singleConnectionManager(connection))
                .setExecutor(Executor.log())
                .setSqlFormatter(SqlFormatter.PRETTY)
                .setDatabaseValidationMode(DatabaseValidationMode.NONE)
                .build();
    }
}
