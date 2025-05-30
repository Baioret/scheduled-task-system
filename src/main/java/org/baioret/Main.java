package org.baioret;

import com.zaxxer.hikari.*;
import org.baioret.config.DataSourceConfig;
import org.baioret.core.schedulable.*;
import org.baioret.core.service.retry.Retry;
import org.baioret.core.service.task.*;
import org.baioret.core.holder.RepositoryHolder;
import org.baioret.core.worker.TaskWorkerPool;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static TaskScheduler taskScheduler;
    private static TaskWorkerPool taskWorkerPool;

    public static void main(String[] args) {

        initSystem();

        Map<String, String> params = Map.of(
                "ID", "4",
                "message", "test params");
        String executionTime = Timestamp.valueOf(LocalDateTime.now()).toString();
        Retry defaultRetryParams = new Retry.RetryBuilder().build();

        taskScheduler.scheduleTask(Task2.class, params, executionTime, defaultRetryParams);


        taskScheduler.cancelTask(100L, Task1.class);

        taskScheduler.scheduleTask(Task2.class, params, executionTime, defaultRetryParams);

        Map<Class<? extends Schedulable>, Integer> workers = Map.of(
                Task1.class, 2,
                Task2.class, 1);
        taskWorkerPool.initWorkers(workers);

        taskWorkerPool.initWorker(Task1.class, 1);

        Optional<List<UUID>> workerIds = taskWorkerPool.
                getWorkersIdByCategory(Task2.class.getSimpleName());
        workerIds.ifPresent(uuids -> taskWorkerPool.
                shutdownWorker(Task2.class.getSimpleName(), uuids.get(0)));
    }

    private static void initSystem() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DataSourceConfig.jdbcUrl);
        config.setUsername(DataSourceConfig.username);
        config.setPassword(DataSourceConfig.password);
        DataSource dataSource = new HikariDataSource(config);

        RepositoryHolder.init(dataSource);
        taskScheduler = new TaskScheduler();
        taskWorkerPool = new TaskWorkerPool();
    }
}