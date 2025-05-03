package com.olive.test;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class DbMetaDataTest {
    /**
     * example: database meta data
     */
    @Test
    public void database_meta_data_test() throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://172.31.50.254:3308/ruoyi_olive",
                "root",
                "root"
        );

        DatabaseMetaData metaData = connection.getMetaData();

        System.out.println(metaData.getCatalogs());
    }
}
