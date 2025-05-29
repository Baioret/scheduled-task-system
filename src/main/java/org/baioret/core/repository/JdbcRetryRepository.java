package org.baioret.core.repository;

import org.baioret.core.entity.RetryParams;

import javax.sql.DataSource;
import java.sql.*;

public class JdbcRetryRepository implements RetryRepository {
    private final DataSource dataSource;
    private final String tableName;
    private final String taskTableName;

    public JdbcRetryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.tableName = "delays_";
        this.taskTableName = "tasks_";
    }

    @Override
    public RetryParams getRetryParams(Long taskId, String category) {

        String sql = "SELECT * FROM " + tableName + category + " WHERE task_id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setLong(1, taskId);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    RetryParams retryParams = new RetryParams(taskId);
                    retryParams.setWithRetry(result.getBoolean("with_retry"));
                    retryParams.setRetryCount(result.getInt("retry_count"));
                    retryParams.setValueIsFixed(result.getBoolean("value_is_fixed"));
                    retryParams.setFixDelayValue(result.getLong("fix_delay_value"));
                    retryParams.setDelayBase(result.getLong("delay_base"));
                    retryParams.setDelayLimit(result.getLong("delay_limit"));
                    return retryParams;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void save(RetryParams retryParams, String category) {

        createTableIfNotExists(category);

        String sql = "INSERT INTO " + tableName + category + " (task_id, with_retry, retry_count, value_is_fixed, fix_delay_value, delay_base, delay_limit)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, retryParams.getTaskId());
            stmt.setBoolean(2, retryParams.isWithRetry());
            stmt.setInt(3, retryParams.getRetryCount());
            stmt.setBoolean(4, retryParams.isValueIsFixed());
            stmt.setLong(5, retryParams.getFixDelayValue());
            stmt.setLong(6, retryParams.getDelayBase());
            stmt.setLong(7, retryParams.getDelayLimit());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to insert row into table " + tableName + category);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists(String category) {

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + category + " (\n" +
                "task_id BIGINT PRIMARY KEY,\n" +
                "    with_retry BOOL NOT NULL,\n" +
                "    retry_count INT,\n" +
                "    value_is_fixed BOOL,\n" +
                "    fix_delay_value BIGINT,\n" +
                "    delay_base BIGINT,\n" +
                "    delay_limit BIGINT,\n" +
                "    FOREIGN KEY (task_id) REFERENCES " + taskTableName + category + " (id) ON DELETE CASCADE);";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
