package org.baioret.core.service.task;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.service.retry.Retry;
import org.baioret.core.validator.RetryValidator;
import org.baioret.core.holder.ServiceHolder;
import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.logging.LogService;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

public class TaskScheduler implements TaskSchedulerService {

    private final TaskManager taskManager;

    public TaskScheduler() {
        this.taskManager = ServiceHolder.getTaskManager();
    }

    @Override
    public <T extends Schedulable> Optional<Long> scheduleTask(Class<T> schedulable, Map<String, String> params, String executionTime, Retry retry) {
        try {
            ScheduledTask savedTask;
            if (retry != null) {
                if (!RetryValidator.validateParams(retry)) {
                    LogService.logger.severe("Retry params validation failed");
                    return Optional.empty();
                }
                savedTask = createAndSaveTask(schedulable, params, executionTime);
                createAndSaveRetryParams(retry, savedTask);
            } else {
                savedTask = createAndSaveTask(schedulable, params, executionTime);
            }

            LogService.logger.info("Task with category '" + schedulable.getSimpleName() + "' has been scheduled with id " + savedTask.getId());
            return Optional.of(savedTask.getId());
        } catch (Exception e) {
            LogService.logger.severe("Failed to schedule task with category " + schedulable.getSimpleName() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T extends Schedulable> Optional<Long> scheduleTask(Class<T> schedulable, Map<String, String> params, String executionTime) {
        return scheduleTask(schedulable, params, executionTime, null);
    }

    private <T extends Schedulable> ScheduledTask createAndSaveTask(Class<T> schedulable, Map<String, String> params, String executionTime) {
        ScheduledTask task = new ScheduledTask();
        String category = schedulable.getSimpleName();
        task.setCategory(category);
        task.setPath(schedulable.getName());
        task.setParams(params);
        task.setExecutionTime(Timestamp.valueOf(executionTime));
        task.setId(taskManager.saveTask(task, category));
        return task;
    }

    private void createAndSaveRetryParams(Retry retry, ScheduledTask task) {
        RetryParams retryParams = new RetryParams(task.getId());
        retryParams.setDelayValueIsFixed(retry.isFixedRetryPolicy());
        retryParams.setMaxAttempts(retry.getMaxRetryCount());
        retryParams.setDelayLimit(retry.getDelayLimit());
        retryParams.setFixedDelayValue(retry.getFixDelayValue());
        retryParams.setDelayBase(retry.getDelayBase());
        taskManager.saveRetryParams(retryParams, task.getCategory());
    }

    @Override
    public <T extends Schedulable> boolean cancelTask(Long id, Class<T> schedulable) {
        ScheduledTask task = taskManager.getTask(id, schedulable.getSimpleName());
        try {
            tryToCancelTask(id, schedulable.getSimpleName(), task);
            return true;
        } catch (Exception e) {
            LogService.logger.severe(String.format("Cannot cancel task with id %s and category '%s': ", id, schedulable) + e.getMessage());
        }
        return false;
    }

    private void tryToCancelTask(Long id, String category, ScheduledTask task) {
        if (task != null) {
            if (task.getStatus() == TaskStatus.PENDING) {
                taskManager.cancelTask(id, category);
                LogService.logger.info(String.format("Task with id %s and category '%s' has been canceled", id, category));
            } else {
                throw new RuntimeException(String.format("Cannot cancel task with id %s and category '%s'. Task status is '%s'", id, category, task.getStatus().name()));
            }
        } else {
            throw new RuntimeException(String.format("Cannot cancel task with id %s and category '%s'. Task not found", id, category));
        }
    }
}