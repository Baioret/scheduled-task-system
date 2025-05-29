package org.baioret.core.service.task;

import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    Long save(ScheduledTask task, String category);

    void cancelTask(Long id, String category);

    void changeTaskStatus(Long id, TaskStatus taskStatus, String category);

    void increaseRetryCountForTask(Long id, String category);

    List<ScheduledTask> getReadyTasksByCategory(String category);

    ScheduledTask getNextReadyTaskByCategory(String category);

    void rescheduleTask(Long id, long delay, String category);

    ScheduledTask getTask(Long id, String category);
}
