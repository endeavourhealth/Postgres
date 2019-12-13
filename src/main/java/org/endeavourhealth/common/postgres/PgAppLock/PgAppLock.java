package org.endeavourhealth.common.postgres.PgAppLock;

import org.apache.commons.lang3.Validate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PgAppLock implements AutoCloseable {

    private String appName;
    private Connection connection;

    public PgAppLock(String appName, Connection connection) throws PgAppLockException {
        Validate.notNull(appName);

        this.appName = "PgAppLock-" + appName;

        try {
            this.connection = connection;
        } catch (Exception e) {
            throw new PgAppLockException("Error obtaining database connection", e);
        }

        getLock();
    }

    private void getLock() throws PgAppLockException {

        try {
            String sql = "select pg_try_advisory_lock(('x' || md5(?))::bit(64)::bigint) as lock_result";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, appName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();

                    if (!resultSet.getBoolean("lock_result"))
                        throw new PgAppLockException("Could not obtain lock for application '" + appName + "'");
                }
            }
        } catch (PgAppLockException e) {
            throw e;
        } catch (Exception e) {
            throw new PgAppLockException("Error while obtaining lock for application '" + appName + "'", e);
        }
    }

    @Override
    public void close() throws Exception {
        releaseLock();
    }

    public void releaseLock() throws PgAppLockException {

        try {
            String sql = "select pg_advisory_unlock(('x' || md5(?))::bit(64)::bigint)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, appName);

                statement.execute();
            }

            this.connection.close();

        } catch (Exception e) {
            throw new PgAppLockException("Error while releasing lock for application '" + appName + "'", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getAppName() {
        return appName;
    }
}
