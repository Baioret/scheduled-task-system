package org.baioret.integrationtest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.baioret.config.DataSourceConfig;
import org.baioret.core.holder.RepositoryHolder;
import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.schedulable.Task1;
import org.baioret.core.schedulable.Task2;
import org.baioret.integrationtest.task.TaskManager;
import org.baioret.integrationtest.task.TaskThreads;
import org.baioret.integrationtest.worker.WorkerManager;
import org.baioret.integrationtest.worker.WorkerThreads;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class WorkerAndTaskIntegrationTest {
    private static final int MAX_WORKER_THREADS = 3;
    private static final int MIN_WORKER_THREADS = 1;
    private static final int WORKER_THREAD_COUNT = 3;
    private static final int TASK_THREAD_COUNT = 5;
    private static final int BOUND_MILLIS_TO_SLEEP = 10000;

    public static void main(String[] args) {
        initDataSource();
        TestThreads workerThreads = createWorkerThreads();
        TestThreads taskThreads = createTaskThreads();
        initThreads(workerThreads, WORKER_THREAD_COUNT);
        initThreads(taskThreads, TASK_THREAD_COUNT);
    }

    private static void initThreads(TestThreads testThreads, int workerThreadCount) {
        testThreads.initThreads(workerThreadCount, BOUND_MILLIS_TO_SLEEP);
        testThreads.stoppingThreads(workerThreadCount, BOUND_MILLIS_TO_SLEEP);
    }

    private static void initDataSource() {
        DataSource dataSource = createDataSource();
        RepositoryHolder.init(dataSource);
    }

    private static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DataSourceConfig.jdbcUrl);
        config.setUsername(DataSourceConfig.username);
        config.setPassword(DataSourceConfig.password);
        return new HikariDataSource(config);
    }

    private static TestThreads createWorkerThreads() {
        WorkerManager workerManager = new WorkerManager(MAX_WORKER_THREADS, MIN_WORKER_THREADS,
                setupClasses());
        return new WorkerThreads(workerManager);
    }

    private static TestThreads createTaskThreads() {
        TaskManager taskManager = new TaskManager(setupClasses());
        return new TaskThreads(taskManager);
    }

    private static Map<Integer, Class<? extends Schedulable>> setupClasses() {
        Map<Integer, Class<? extends Schedulable>> classes = new HashMap<>();
        classes.put(0, Task1.class);
        classes.put(1, Task2.class);
        return classes;
    }
}
