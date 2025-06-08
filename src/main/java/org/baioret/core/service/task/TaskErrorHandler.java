package org.baioret.core.service.task;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.logging.LogService;

import static org.baioret.core.service.retry.DelayCalculator.getNextDelay;

public class TaskErrorHandler {

    private final TaskManager taskManager;

    public TaskErrorHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void tryToReschedule(ScheduledTask task) {
        RetryParams retryParams = taskManager.getRetryParams(task.getId(), task.getCategory());
        if (retryParams != null) {
            if (task.getAttempt() < retryParams.getMaxAttempts()) {
                taskManager.increaseAttemptsCountForTask(task.getId(), task.getCategory());
                taskManager.changeTaskStatus(task.getId(), TaskStatus.RETRYING, task.getCategory());
                applyRetryPolicy(task, retryParams);
            } else {
                LogService.logger.info(String.format("No retry attempts for task with id %s and category '%s'",
                        task.getId(), task.getCategory()));
                taskManager.changeTaskStatus(task.getId(), TaskStatus.FAILED, task.getCategory());
            }
        } else {
            LogService.logger.info(String.format("Task with id %s and category '%s' was scheduled without retry",
                    task.getId(), task.getCategory()));
            taskManager.changeTaskStatus(task.getId(), TaskStatus.FAILED, task.getCategory());
        }
    }

    private void applyRetryPolicy(ScheduledTask task, RetryParams retryParams) {
        if (retryParams.delayValueIsFixed())
            handleFixedRetryPolicy(task, retryParams);
        else
            handleExponentialRetryPolicy(task, retryParams);
    }

    private void handleFixedRetryPolicy(ScheduledTask task, RetryParams retryParams) {
        try {
            taskManager.rescheduleTask(task.getId(), retryParams.getFixedDelayValue(), task.getCategory());
        } catch (Exception e) {
            LogService.logger.severe(e.getMessage());
        }
    }

    private void handleExponentialRetryPolicy(ScheduledTask task, RetryParams retryParams) {
        long delayValue = getNextDelay(task.getAttempt(), retryParams.getDelayBase(), retryParams.getDelayLimit());
        if (delayValue > 0) {
            try {
                taskManager.rescheduleTask(task.getId(), delayValue, task.getCategory());
            } catch (Exception e) {
                LogService.logger.severe(e.getMessage());
            }
        } else {
            LogService.logger.info(String.format("Task with id %s and category '%s' reached delay limit",
                    task.getId(), task.getCategory()));
            taskManager.changeTaskStatus(task.getId(), TaskStatus.FAILED, task.getCategory());
        }
    }
}
