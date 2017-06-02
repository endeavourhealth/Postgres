package org.endeavourhealth.common.postgres;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class PgDataSource {
    public static DataSource get(String jdbcUrl, String username, String password) throws SQLException {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setUrl(jdbcUrl);
        pgSimpleDataSource.setUser(username);
        pgSimpleDataSource.setPassword(password);

        return pgSimpleDataSource;
    }
}
