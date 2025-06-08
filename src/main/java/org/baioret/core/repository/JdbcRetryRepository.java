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
                    retryParams.setMaxAttempts(result.getInt("max_attempts"));
                    retryParams.setDelayValueIsFixed(result.getBoolean("value_is_fixed"));
                    retryParams.setFixedDelayValue(result.getLong("fixed_delay_value"));
                    retryParams.setDelayBase(result.getDouble("delay_base"));
                    retryParams.setDelayLimit(result.getLong("delay_limit"));
                    return retryParams;
                } else return null;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(RetryParams retryParams, String category) {

        createTableIfNotExists(category);

        String sql = "INSERT INTO " + tableName + category + " (task_id, max_attempts, value_is_fixed, fixed_delay_value, delay_base, delay_limit)" +
                " VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setLong(1, retryParams.getTaskId());
            stmt.setInt(2, retryParams.getMaxAttempts());
            stmt.setBoolean(3, retryParams.delayValueIsFixed());
            stmt.setLong(4, retryParams.getFixedDelayValue());
            stmt.setDouble(5, retryParams.getDelayBase());
            stmt.setLong(6, retryParams.getDelayLimit());

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
                "    max_attempts INT,\n" +
                "    value_is_fixed BOOL,\n" +
                "    fixed_delay_value BIGINT,\n" +
                "    delay_base DOUBLE,\n" +
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
