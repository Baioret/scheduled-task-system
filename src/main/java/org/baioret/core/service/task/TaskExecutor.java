package org.baioret.core.service.task;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.logging.LogService;
import org.baioret.core.service.retry.RetryService;

import java.util.HashMap;
import java.util.Map;

import static org.baioret.core.service.retry.DelayCalculator.getNextDelay;

public class TaskExecutor {
    private final TaskService taskService;

    private final RetryService retryService;

    Map<Long, Boolean> isTaskRescheduled = new HashMap<>();


    public TaskExecutor(TaskService taskService, RetryService retryService) {
        this.taskService = taskService;
        this.retryService = retryService;
    }

    private void fixedRetryPolicy(Long id, String category) {
        long fixDelayValue = retryService.getRetryParams(id, category).getFixDelayValue();
        if (fixDelayValue >= 0) {
            taskService.rescheduleTask(id, fixDelayValue, category);
        } else {
            taskService.changeTaskStatus(id, TaskStatus.FAILED, category);
            throw new RuntimeException(String.format("Can`t reschedule task with id %s and category '%s'. Value of delay = %s it can`t be < 0",
                    id, category, fixDelayValue));
        }
    }

    private void exponentialRetryPolicy(Long id, int retryCount, double delayBase, long limit, String category) {
        long delayValue = delayBase > 0 ? getNextDelay(retryCount, delayBase, limit) : getNextDelay(retryCount, limit);
        if (delayValue >= 0) {
            taskService.rescheduleTask(id, delayValue, category);
        } else {
            taskService.changeTaskStatus(id, TaskStatus.FAILED, category);
            throw new RuntimeException(String.format("Can`t reschedule task with id %s and category '%s'. Value of delay = %s it can`t be > limit = %s",
                    id, category, delayValue, limit));
        }
    }

    private void applyRetryPolicy(Long id, int retryCount, RetryParams retryParams, String category) {
        if (retryParams.isWithRetry()) {
            if (retryParams.isValueIsFixed()) {
                fixedRetryPolicy(id, category);
            } else {
                exponentialRetryPolicy(id, retryCount, retryParams.getDelayBase(), retryParams.getDelayLimit(), category);
            }
        } else {
            LogService.logger.warning(String.format("Can`t get retry params. Retry for task with id %s and category '%s' is turned off", id, category));
        }
    }

    public void executeRetryPolicyForTask(Long id, String category, int retryCount) {
        RetryParams retryParams = retryService.getRetryParams(id, category);
        if (retryParams.isWithRetry()) {
            int maxRetryCount = retryParams.getRetryCount();
            if (isTaskRescheduled.containsKey(id)) {
                if (isTaskRescheduled.get(id).equals(true)) {

                    if (retryCount < maxRetryCount - 1) {
                        applyRetryPolicy(id, retryCount, retryParams, category);
                        taskService.increaseRetryCountForTask(id, category);
                    } else {
                        taskService.increaseRetryCountForTask(id, category);
                        LogService.logger.info(String.format("The attempts for retry execute task with id %s and category '%s' are over. ",
                                id, category));
                        taskService.changeTaskStatus(id, TaskStatus.FAILED, category);
                    }
                    return;
                }
            }
            isTaskRescheduled.put(id, true);
            applyRetryPolicy(id, retryCount, retryParams, category);
            LogService.logger.info(String.format("Retrying task with id %s and category '%s'. Current attempt = %s",
                    id, category, retryCount + 1));
            return;
        }
        taskService.changeTaskStatus(id, TaskStatus.FAILED, category);
    }
}
