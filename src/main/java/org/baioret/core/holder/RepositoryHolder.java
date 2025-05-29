package org.baioret.core.holder;

import org.baioret.core.repository.JdbcRetryRepository;
import org.baioret.core.repository.JdbcTaskRepository;
import org.baioret.core.repository.RetryRepository;
import org.baioret.core.repository.TaskRepository;

import javax.sql.DataSource;

public class RepositoryHolder {
    private static volatile DataSource dataSource;

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void init(DataSource ds) {
        if (dataSource != null) {
            throw new IllegalStateException("DataSource has already been initialized");
        }

        dataSource = ds;
    }

    private static class TaskRepositoryHolderInstance {
        static final TaskRepository INSTANCE = new JdbcTaskRepository(dataSource);
    }

    public static TaskRepository getTaskInstance() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource has not been initialized");
        }
        return TaskRepositoryHolderInstance.INSTANCE;
    }

    private static class RetryRepositoryHolderInstance {
        static final RetryRepository INSTANCE = new JdbcRetryRepository(dataSource);
    }

    public static RetryRepository getRetryInstance() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource has not been initialized");
        }
        return RetryRepositoryHolderInstance.INSTANCE;
    }
}
