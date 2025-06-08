package org.baioret.integrationtest.task;

import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.service.retry.Retry;
import org.baioret.core.service.task.TaskScheduler;
import org.baioret.core.service.task.TaskSchedulerService;
import org.baioret.integrationtest.EuropeanDateFormatter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskManager {
    private static final Random RANDOM = new Random();

    private final TaskSchedulerService taskScheduler;
    private final Map<Integer, Class<? extends Schedulable>> classes;
    private final Map<Integer, Retry> retries = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> tasksId = new ConcurrentHashMap<>();

    public TaskManager(Map<Integer, Class<? extends Schedulable>> classes) {
        this.taskScheduler = new TaskScheduler();
        this.classes = classes;
        initRetryParams();
    }

    private void initRetryParams() {
        Retry defaultRetryParams = new Retry.RetryBuilder().setMaxAttemptsCount(1).setDelayLimit(1L).build();
        Retry fixedRetryParams = new Retry.RetryBuilder().setFixedRetryPolicy(true).setFixDelayValue(60000L).setMaxAttemptsCount(3).build();
        Retry exponentialRetryParams = new Retry.RetryBuilder().setMaxAttemptsCount(3).setDelayLimit(100000L).build();

        retries.put(0, defaultRetryParams);
        retries.put(1, fixedRetryParams);
        retries.put(2, exponentialRetryParams);
    }

    public void initRandomTask() {
        Class<? extends Schedulable> randomClass = classes.get(RANDOM.nextInt(classes.size()));
        String executionTime = Timestamp.valueOf(LocalDateTime.now().plusSeconds(10)).toString();
        Retry randomRetryParams = retries.get(RANDOM.nextInt(retries.size()));
        Map<String, String> randomParams = new HashMap<>();
        Random rand = new Random();
        randomParams.put("ID", (rand.nextInt(5000) + 1) + "");
        randomParams.put("message", "Test message");
        Long taskId = scheduleTask(randomClass, randomParams, executionTime, randomRetryParams);
        putInTasksId(randomClass, taskId);
        printScheduledTaskInfo(randomClass.getSimpleName(), executionTime, taskId);
    }

    private Long scheduleTask(Class<? extends Schedulable> randomClass, Map<String, String> params, String executionTime, Retry randomRetryParams) {
        return taskScheduler.scheduleTask(randomClass, params, executionTime, randomRetryParams)
                .orElseThrow(() -> new RuntimeException("Could not schedule task with category " +
                        randomClass.getSimpleName() + " and retry params " +
                        randomRetryParams.toString()));
    }

    private void putInTasksId(Class<? extends Schedulable> randomClass, Long taskId) {
        if (tasksId.containsKey(randomClass.getSimpleName())) {
            tasksId.get(randomClass.getSimpleName()).add(taskId);
        } else {
            List<Long> taskIds = new CopyOnWriteArrayList<>();
            taskIds.add(taskId);
            tasksId.put(randomClass.getSimpleName(), taskIds);
        }
    }

    private void printScheduledTaskInfo(String className, String executionTime, Long taskId) {
        System.out.println("Создана новая задача:" +
                "\nКласс - " + className +
                "\nId - " + taskId +
                "\nВремя выполнения - " + executionTime +
                "\nДата создания - " + getStringTime(LocalDateTime.now()) + "\n");
    }

    public void cancelRandomTask() {
        Class<? extends Schedulable> randomCategory = classes.get(RANDOM.nextInt(classes.size()));
        List<Long> tasksIdByCategory = tasksId.get(randomCategory.getSimpleName());
        if (tasksIdByCategory != null) {
            Long randomTaskId = tasksIdByCategory.get(RANDOM.nextInt(tasksIdByCategory.size()));
            if (taskScheduler.cancelTask(randomTaskId, randomCategory)) {
                printCanceledTaskInfo(randomCategory.getSimpleName(), randomTaskId);
            }
            else System.out.println("Задачу [" + randomCategory.getSimpleName() + "] с id " + randomTaskId + " не удалось отменить: статус задачи не 'PENDING'\n");
        }
    }

    private String getStringTime(LocalDateTime time) {
        return EuropeanDateFormatter.getFromLocalDateTime(time);
    }

    private void printCanceledTaskInfo(String className, Long taskId) {
        System.out.println("Отменена задача:" +
                "\nId: " + taskId +
                "\nКатегория - " + className +
                "\nДата отмены - " + getStringTime(LocalDateTime.now()) + "\n");
    }
}