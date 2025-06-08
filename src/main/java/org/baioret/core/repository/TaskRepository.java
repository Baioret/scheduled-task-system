package org.baioret.core.repository;

import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;

import java.util.List;

public interface TaskRepository {
    Long save(ScheduledTask task, String category);

    void changeTaskStatus(Long id, TaskStatus status, String category);

    void increaseRetryCountForTask(Long id, String category);

    List<ScheduledTask> getReadyTasksByCategory(String category);

    ScheduledTask getNextReadyTaskByCategory(String category);

    void rescheduleTask(Long id, long delay, String category);

    ScheduledTask findById(Long id, String category);

    boolean existsById(Long id, String category);
}