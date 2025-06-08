package org.baioret.core.service.task;

import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.logging.LogService;
import org.baioret.core.repository.TaskRepository;

import java.sql.Timestamp;
import java.util.List;

public class DatabaseTaskService implements TaskService {
    private final TaskRepository taskRepository;

    public DatabaseTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public ScheduledTask getTask(Long id, String category) {
        ScheduledTask task = taskRepository.findById(id, category);
        if (task != null) {
            return task;
        } else {
            throw new RuntimeException(String.format("Can`t get task with id %s and category '%s'. Task not found", id, category));
        }
    }

    @Override
    public void changeTaskStatus(Long id, TaskStatus taskStatus, String category) {
        TaskStatus currentStatus = getTask(id, category).getStatus();
        if (!(currentStatus.equals(taskStatus))) {
            taskRepository.changeTaskStatus(id, taskStatus, category);
            LogService.logger.info(String.format("Status for task with id %s and category '%s' changed from '%s' to '%s'",
                    id, category, currentStatus.name(), taskStatus.name()));
        }
    }

    @Override
    public void increaseAttemptsCountForTask(Long id, String category) {
        taskRepository.increaseRetryCountForTask(id, category);
    }

    @Override
    public void rescheduleTask(Long id, long delay, String category) {
        if (delay >= 0) {
            Timestamp time = new Timestamp(getTask(id, category).getExecutionTime().getTime() + delay);
            taskRepository.rescheduleTask(id, delay, category);
            LogService.logger.info(String.format("Reschedule task with id %s and category '%s'. New execution time = %s",
                    id, category, time));
            taskRepository.changeTaskStatus(id, TaskStatus.PENDING, category);
        } else {
            throw new RuntimeException(String.format("Can`t reschedule task with id %s and category '%s'. Value of delay < 0", id, category));
        }
    }

    @Override
    public List<ScheduledTask> getReadyTasksByCategory(String category) {
        return taskRepository.getReadyTasksByCategory(category);
    }

    @Override
    public ScheduledTask getNextReadyTaskByCategory(String category) {
        return taskRepository.getNextReadyTaskByCategory(category);
    }

    @Override
    public Long save(ScheduledTask task, String category) {
        Long id = taskRepository.save(task, category);
        LogService.logger.info(String.format("Task with id %s and category %s has been created", id, category));
        return id;
    }
}
