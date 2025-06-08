package org.baioret.core.service.task;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.service.retry.RetryService;

public class TaskManager {

    private final TaskService taskService;
    private final RetryService retryService;

    public TaskManager(TaskService taskService, RetryService retryService) {
        this.taskService = taskService;
        this.retryService = retryService;
    }

    public RetryParams getRetryParams(Long taskId, String category) {
        return retryService.getRetryParams(taskId, category);
    }

    public void saveRetryParams(RetryParams params, String category) {
        retryService.save(params, category);
    }

    public ScheduledTask getTask(Long id, String category) {
        return taskService.getTask(id, category);
    }

    public Long saveTask(ScheduledTask task, String category) {
        return taskService.save(task, category);
    }

    public void cancelTask(Long id, String category) {
        changeTaskStatus(id, TaskStatus.CANCELED, category);
    }

    public void changeTaskStatus(Long id, TaskStatus status, String category) {
        taskService.changeTaskStatus(id, status, category);
    }

    public void increaseAttemptsCountForTask(Long taskId, String category) {
        taskService.increaseAttemptsCountForTask(taskId, category);
    }

    public ScheduledTask getNextReadyTaskByCategory(String category) {
        return taskService.getNextReadyTaskByCategory(category);
    }

    public void rescheduleTask(Long id, long delay, String category) {
        taskService.rescheduleTask(id, delay, category);
    }
}
