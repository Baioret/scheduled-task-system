package org.baioret.core.worker;

import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.holder.ExecutorHolder;
import org.baioret.core.holder.ServiceHolder;
import org.baioret.core.logging.LogService;
import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.service.task.TaskExecutor;
import org.baioret.core.service.task.TaskService;

import java.util.Map;
import java.util.UUID;

public class TaskWorker implements Runnable {
    private final String category;

    private final UUID workerId;
    private final TaskService taskService;
    private final TaskExecutor taskExecutor;

    private boolean doStop = false;

    public TaskWorker(String category, UUID workerId) {
        this.workerId = workerId;
        this.taskService = ServiceHolder.getTaskService();
        this.taskExecutor = ExecutorHolder.getTaskExecutor();
        this.category = category;
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return !this.doStop;
    }

    private synchronized boolean executeTask(Schedulable task, Map<String, String> params) {
        return task.execute(params);
    }

    @Override
    public void run() {
        try {
            while (keepRunning()) {
                Thread.sleep(3000);
                LogService.logger.info(String.format("Worker with id %s and category '%s' is searching ready tasks...",
                        workerId, category));
                ScheduledTask nextTask = null;
                try {
                    nextTask = taskService.getNextReadyTaskByCategory(category);
                } catch (Exception e) {
                    LogService.logger.severe(e.getMessage());
                }
                if (nextTask != null) {
                    LogService.logger.info(String.format("Worker %s start execute task with id: %s and category '%s'",
                            workerId, nextTask.getId(), category));
                    Schedulable taskClass = (Schedulable) Class.forName(nextTask.getPath()).getDeclaredConstructor().newInstance();
                    if (executeTask(taskClass, nextTask.getParams())) {
                        taskService.changeTaskStatus(nextTask.getId(), TaskStatus.COMPLETED, category);
                        LogService.logger.info(String.format("Task with id %s and category '%s' has been executed.",
                                nextTask.getId(), category));
                    } else {
                        LogService.logger.info(String.format("Task with id %s and category '%s' has been failed.",
                                nextTask.getId(), category));
                        taskService.changeTaskStatus(nextTask.getId(), TaskStatus.RETRYING, category);
                        taskExecutor.executeRetryPolicyForTask(nextTask.getId(), nextTask.getCategory(), nextTask.getRetryCount());
                    }
                }
            }
            LogService.logger.info(String.format("Worker with id %s and category '%s' stopped", workerId, category));
        } catch (Exception e) {
            LogService.logger.severe(e.getMessage());
        }
    }
}
