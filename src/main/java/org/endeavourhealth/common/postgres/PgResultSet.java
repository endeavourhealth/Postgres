package org.endeavourhealth.common.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class PgResultSet {
    // make static for now
    public static Integer getInteger(ResultSet resultSet, String columnName) throws SQLException {
        int result = resultSet.getInt(columnName);

        if (resultSet.wasNull())
            return null;

        return result;
    }

    public static LocalDateTime getLocalDateTime(ResultSet resultSet, String columnName) throws SQLException {
        Date sqlDate = resultSet.getDate(columnName);

        if (resultSet.wasNull())
            return null;

        Date date = new Date(sqlDate.getTime());
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
