package org.baioret.core.service.task;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.service.retry.Retry;
import org.baioret.core.service.retry.RetryService;
import org.baioret.core.validator.RetryValidator;
import org.baioret.core.holder.ServiceHolder;
import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.logging.LogService;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

public class TaskScheduler implements TaskSchedulerService {
    private final TaskService taskService;

    private final RetryService retryService;

    public TaskScheduler() {
        this.taskService = ServiceHolder.getTaskService();
        this.retryService = ServiceHolder.getRetryService();
    }

    @Override
    public <T extends Schedulable> Optional<Long> scheduleTask(Class<T> scheduleClass, Map<String, String> params, String executionTime, Retry retry) {
        try {
            LogService.logger.info("Process scheduleTask for '" + scheduleClass.getName() + "' started");
            if (!RetryValidator.validateParams(retry)) {
                throw new RuntimeException("Delay params validation failed");
            }
            ScheduledTask savedTask = createAndSaveTask(scheduleClass, params, executionTime);
            createAndSaveRetryParams(retry, savedTask);

            LogService.logger.info("Process scheduleTask has been completed. Returns id for task: " + savedTask.getId());
            return Optional.of(savedTask.getId());
        } catch (Exception e) {
            LogService.logger.severe("Process schedule task failed. " + e.getMessage());
            return Optional.empty();
        }
    }

    private <T extends Schedulable> ScheduledTask createAndSaveTask(Class<T> scheduleClass, Map<String, String> params, String executionTime) {
        ScheduledTask task = new ScheduledTask();
        String category = scheduleClass.getSimpleName();
        task.setCategory(category);
        task.setPath(scheduleClass.getName());
        task.setParams(params);
        task.setExecutionTime(Timestamp.valueOf(executionTime));
        task.setId(taskService.save(task, category));
        return task;
    }

    private void createAndSaveRetryParams(Retry retry, ScheduledTask task) {
        RetryParams retryParams = new RetryParams(task.getId());
        retryParams.setWithRetry(retry.isWithRetry());
        retryParams.setValueIsFixed(retry.isFixedRetryPolicy());
        retryParams.setRetryCount(retry.getMaxRetryCount());
        retryParams.setDelayLimit(retry.getDelayLimit());
        retryParams.setFixDelayValue(retry.getFixDelayValue());
        retryParams.setDelayBase(retry.getDelayBase());
        retryService.save(retryParams, task.getCategory());
    }

    @Override
    public void cancelTask(Long id, String category) {
        LogService.logger.info(String.format("Process cancel task with id: %s and category: '%s' started", id, category));
        ScheduledTask task = taskService.getTask(id, category);
        try {
            tryToCancelTask(id, category, task);
        } catch (Exception e) {
            LogService.logger.severe(String.format("Process cancel task with id: %s and category: '%s' has been failed. ", id, category) + e.getMessage());
        }
    }

    private void tryToCancelTask(Long id, String category, ScheduledTask task) {
        if (task != null) {
            if (task.getStatus() == TaskStatus.PENDING) {
                taskService.cancelTask(id, category);
                LogService.logger.info(String.format("Process cancel Task with id: %s and category: '%s' completed. Task has been canceled successfully", id, category));
            } else {
                throw new RuntimeException(String.format("Cannot cancel task with id: %s and category: '%s'. Task status is '%s'", id, category, task.getStatus().name()));
            }
        } else {
            throw new RuntimeException(String.format("Cannot cancel task with id: %s and category: '%s'. Task not found", id, category));
        }
    }
}
