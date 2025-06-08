package org.baioret.core.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTaskRepository implements TaskRepository {

    private final DataSource dataSource;
    private final String tableName = "tasks_";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcTaskRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean existsById(Long id, String category) {

        return findById(id, category) != null;
    }

    @Override
    public Long save(ScheduledTask task, String category) {

        createTableIfNotExists(category);
        createUpdateEvent(category);

        String sql = "INSERT INTO " + tableName + category +
                " (category, path, params, status, execution_time) " +
                "VALUES (?,?,?,?,?)";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, task.getCategory());
            stmt.setString(2, task.getPath());
            stmt.setString(3, objectMapper.writeValueAsString(task.getParams()));
            stmt.setString(4, task.getStatus().name());
            stmt.setTimestamp(5, task.getExecutionTime());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to insert row into " + tableName + category);
            }

            try {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Saving task failed, no ID obtained.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to save task: ", e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void createTableIfNotExists(String category) {

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + category + " (\n" +
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    category VARCHAR(50) NOT NULL,\n" +
                "    path VARCHAR(255) NOT NULL,\n" +
                "    params JSON NOT NULL,\n" +
                "    status ENUM('PENDING','RETRYING','READY','PROCESSING','FAILED','COMPLETED','CANCELED','NONE') NOT NULL DEFAULT 'NONE',\n" +
                "    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    attempt INT DEFAULT 1,\n" +
                "    revision INT NOT NULL DEFAULT 1);";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void createUpdateEvent(String category) {

        String sql1 = "CREATE EVENT IF NOT EXISTS auto_update_" + tableName + category +
                " ON SCHEDULE EVERY 5 SECOND" +
                " DO" +
                " UPDATE " + tableName + category +
                " SET status = 'READY'" +
                " WHERE execution_time <= NOW()" +
                " AND status IN ('PENDING', 'NONE');";

        String sql2 = "SET GLOBAL event_scheduler = ON";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt1 = connection.prepareStatement(sql1);
            stmt1.executeUpdate();

            PreparedStatement stmt2 = connection.prepareStatement(sql2);
            stmt2.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeTaskStatus(Long id, TaskStatus status, String category) {

        String sql = "UPDATE " + tableName + category + " SET status = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setString(1, status.name());
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to change task status with ID " + id + " to " + status.name());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void changeTaskStatus(Long id, TaskStatus status, String category, Connection connection) {

        String sql = "UPDATE " + tableName + category + " SET status = ? WHERE id = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, status.name());
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to change task status with ID " + id + " to " + status.name());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void increaseRetryCountForTask(Long id, String category) {

        String sql = "UPDATE " + tableName + category + " SET attempt = attempt + 1 WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to increase attempt for task with ID: " + id);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ScheduledTask getNextReadyTaskByCategory(String category) {
        return getNextTaskByCategoryWithOptimisticLocking(category);
    }


    @Override
    public List<ScheduledTask> getReadyTasksByCategory(String category) {

        List<ScheduledTask> tasks = new ArrayList<>();
        ScheduledTask currentTask;

        String sql = "SELECT * FROM " + tableName + category + " WHERE status = 'READY' LIMIT 5 FOR UPDATE SKIP LOCKED";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            connection.setAutoCommit(false);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    currentTask = createTaskFromResult(result);
                    changeTaskStatus(currentTask.getId(), TaskStatus.PROCESSING, category, connection);
                    tasks.add(currentTask);
                }
                connection.commit();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }


    private ScheduledTask createTaskFromResult(ResultSet result) {

        ScheduledTask task = new ScheduledTask();

        try {
            task.setId(result.getLong(1));
            task.setCategory(result.getString(2));
            task.setPath(result.getString(3));
            task.setParams(objectMapper.readValue(result.getString(4), new TypeReference<>() {
            }));
            task.setStatus(TaskStatus.valueOf(result.getString(5)));
            task.setExecutionTime(result.getTimestamp(6));
            task.setAttempt(result.getInt(7));

            return task;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> getTaskAndRevisionFromResult(ResultSet result) {

        try {
            ScheduledTask task = createTaskFromResult(result);
            int revision = result.getInt(8);
            return List.of(task, revision);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ScheduledTask getNextTaskByCategoryWithPessimisticLocking(String category) {

        String sql = "SELECT * FROM " + tableName + category + " WHERE status = 'READY' ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    ScheduledTask task = createTaskFromResult(result);
                    changeTaskStatus(task.getId(), TaskStatus.PROCESSING, category, connection);
                    connection.commit();
                    return task;
                }
                else {
                    connection.rollback();
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Task locking error. %s", e));
        }
    }

    private ScheduledTask getNextTaskByCategoryWithOptimisticLocking(String category) {

        String sql;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            sql = "SELECT * FROM " + tableName + category + " WHERE status = 'READY' ORDER BY id LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(sql);

            List<Object> taskAndRevision;
            ScheduledTask task;
            int revision;

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    taskAndRevision = getTaskAndRevisionFromResult(result);
                    task = (ScheduledTask) taskAndRevision.get(0);
                    revision = (int) taskAndRevision.get(1);

                    if (task != null) {
                        changeTaskStatus(task.getId(), TaskStatus.PROCESSING, category, connection);
                    } else {
                        connection.rollback();
                        throw new RuntimeException("Task updating error: task not found");
                    }
                } else {
                    connection.rollback();
                    throw new RuntimeException("No ready tasks in category '" + category + "'");
                }
            }

            sql = "UPDATE " + tableName + category + " SET revision = revision + 1 WHERE id = ? AND revision = ?";

            stmt = connection.prepareStatement(sql);
            stmt.setLong(1, task.getId());
            stmt.setInt(2, revision);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
            } else {
                connection.commit();
                return task;
            }

        } catch (SQLException e) {
            throw new RuntimeException(String.format("Task locking error. %s", e));
        }

        return null;
    }


    @Override
    public void rescheduleTask(Long id, long delay, String category) {

        String sql = "UPDATE " + tableName + category + " SET execution_time = TIMESTAMPADD(MICROSECOND, ?, execution_time) WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, delay * 1000);
            preparedStatement.setLong(2, id);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ScheduledTask findById(Long id, String category) {

        String sql = "SELECT * FROM " + tableName + category + " WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setLong(1, id);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return createTaskFromResult(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
