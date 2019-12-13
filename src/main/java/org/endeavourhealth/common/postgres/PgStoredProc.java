package org.endeavourhealth.common.postgres;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PgStoredProc {

    public interface IResultSetPopulator<T> {
        T populate(ResultSet resultSet) throws SQLException;
    }

    private Connection connection;
    private String storedProcedureName;
    private Map<String, Object> parameters;
    private Map<String, Object> outParameters;
    private Statement multiStatement;
    private ResultSet multiResultSet;

    public PgStoredProc(Connection connection) {
        if (connection == null)
            throw new IllegalArgumentException("connection is null");

        this.connection = connection;
        this.parameters = new HashMap<>();
        this.outParameters = new HashMap<>();
        //this.multiConnection = null;
        this.multiStatement = null;
        this.multiResultSet = null;
    }

    public PgStoredProc setName(String storedProcedureName) {
        this.storedProcedureName = storedProcedureName;
        return this;
    }

    public PgStoredProc addParameter(String name, String value, int truncateToMaxLength) {
        String truncatedValue = value;

        if (truncatedValue != null)
            if (truncatedValue.length() > truncateToMaxLength)
                truncatedValue = truncatedValue.substring(0, truncateToMaxLength);

        return addParameter(name, truncatedValue);
    }

    public <T> PgStoredProc addParameter(String name, T value) {
        this.parameters.put(name, value);
        return this;
    }

    public void execute() throws Exception {
        List<HashMap<String, Object>> outParameters = executeQuery((resultSet) -> {
            HashMap<String, Object> hashMap = new HashMap<>();

            for (int i = 1; i < (resultSet.getMetaData().getColumnCount() + 1); i++)
                hashMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));

            return hashMap;
        });

        this.outParameters = outParameters.get(0);
    }

    public Object getOutParameter(String name) {
        return this.outParameters.get(name);
    }

    public <T extends Object> T executeSingleOrEmptyRow(IResultSetPopulator<T> rowMapper) throws Exception {
        List<T> resultList = executeQuery(rowMapper);

        if (resultList == null)
            throw new PgStoredProcException("No resultset returned (null list)");

        if (resultList.size() == 0)
            return null;

        if (resultList.size() > 1)
            throw new PgStoredProcException("More than one result returned");

        return resultList.get(0);
    }

    public <T extends Object> T executeSingleRow(IResultSetPopulator<T> rowMapper) throws Exception {
        List<T> resultList = executeQuery(rowMapper);

        if (resultList == null)
            throw new PgStoredProcException("No resultset returned (null list)");

        if (resultList.size() == 0)
            throw new PgStoredProcException("No results returned");

        if (resultList.size() > 1)
            throw new PgStoredProcException("More than one result returned");

        return resultList.get(0);
    }

    public <T extends Object> List<T> executeQuery(IResultSetPopulator<T> rowMapper) throws Exception {
        try {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(getFormattedQuery())) {
                    return populatePojo(resultSet, rowMapper);
                }
            }
        } catch (Exception e) {
            throw new PgStoredProcException("executeQuery error, see inner exception", e);

        } finally {
            connection.close();
        }
    }

    public <T extends Object> T executeMultiQuerySingleRow(IResultSetPopulator<T> firstRowMapper) throws PgStoredProcException {
        List<T> resultList;

        try {
            resultList = executeMultiQuery(firstRowMapper);
        } catch (PgStoredProcException e) {
            throw new PgStoredProcException("executeMultiQuerySingleRow error, see inner exception", e.getCause());
        }

        if (resultList == null)
            throw new PgStoredProcException("No resultset returned (null list)");

        if (resultList.size() == 0)
            throw new PgStoredProcException("No results returned");

        if (resultList.size() > 1)
            throw new PgStoredProcException("More than one result returned");

        return resultList.get(0);
    }

    public <T extends Object> T executeMultiQuerySingleOrEmptyRow(IResultSetPopulator<T> firstRowMapper) throws PgStoredProcException {
        List<T> resultList;

        try {
            resultList = executeMultiQuery(firstRowMapper);
        } catch (PgStoredProcException e) {
            throw new PgStoredProcException("executeMultiQuerySingleOrEmptyRow error, see inner exception", e.getCause());
        }

        if (resultList == null)
            throw new PgStoredProcException("No resultset returned (null list)");

        if (resultList.size() == 0)
            return null;

        if (resultList.size() > 1)
            throw new PgStoredProcException("More than one result returned");

        return resultList.get(0);
    }

    public <T extends Object> List<T> executeMultiQuery(IResultSetPopulator<T> firstRowMapper) throws PgStoredProcException {
        try {
            if (multiResultSet == null) {
                //this.multiConnection = this.dataSource.getConnection();
                this.connection.setAutoCommit(false); //need to turn this off for the multi-result set stuff to work
                this.multiStatement = this.connection.createStatement();
                this.multiResultSet = this.multiStatement.executeQuery(getFormattedQuery());

                if (!this.multiResultSet.next())
                    throw new PgStoredProcException("No resultsets found");
            }

            List<T> result = null;
            try (ResultSet resultSet = (ResultSet)this.multiResultSet.getObject(1)) {
                result = populatePojo(resultSet, firstRowMapper);
            }

            //if hte last result set, close everything down
            if (!this.multiResultSet.next()) {
                this.connection.commit();
                closeMultiResources();
            }

            return result;
        } catch (Exception e) {
            closeMultiResourcesQuietly();

            throw new PgStoredProcException("executeMultiQuery error, see inner exception", e);
        }
    }

    private static <T extends Object> List<T> populatePojo(ResultSet resultSet, IResultSetPopulator<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (resultSet.next())
            results.add(rowMapper.populate(resultSet));

        return results;
    }


    private String getFormattedQuery() {
        if (StringUtils.isEmpty(storedProcedureName))
            throw new IllegalArgumentException("storedProcedureName is empty");

        String sql = "select * from " + storedProcedureName
                + "("
                + getFormattedParameters()
                + ");";

        return sql;
    }

    private String getFormattedParameters() {
        List<String> formattedParameters = new ArrayList<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet())
            formattedParameters.add(entry.getKey() + " := " + getFormattedParameterValue(entry.getValue()));

        return StringUtils.join(formattedParameters, ", ");
    }

    private static String getFormattedParameterValue(Object value) {
        if ((value instanceof Integer) || (value instanceof Long))
            return value.toString();
        else if (value instanceof Character)
            return "'" + value + "'";
        else if (value instanceof String)
            return "'" + ((String)value).replace("'", "''") + "'";
        else if ((value instanceof Boolean))
            return value.toString();
        else if (value instanceof java.time.LocalDate)
            return "'" + ((java.time.LocalDate)value).format(DateTimeFormatter.ISO_DATE) + "'";
        else if (value instanceof java.time.LocalDateTime)
            return "'" + ((java.time.LocalDateTime)value).format(DateTimeFormatter.ISO_DATE_TIME) + "'";
        else if (value instanceof java.util.UUID)
            return "'" + value.toString() + "'::uuid";
        else if (value == null)
            return "null";

        throw new NotImplementedException("Parameter type not supported");
    }

    private void closeMultiResourcesQuietly() {
        try {
            closeMultiResources();
        } catch (Exception e) {
            // log
        }
    }

    private void closeMultiResources() throws SQLException {
        if (this.multiResultSet != null
                && !this.multiResultSet.isClosed()) {
            this.multiResultSet.close();
        }

        if (this.multiStatement != null
                && !this.multiStatement.isClosed()) {
            this.multiStatement.close();
        }

        this.connection.close();

        /*if (this.multiConnection != null)
            if (!this.multiConnection.isClosed())
                this.multiConnection.close();*/
    }
}
