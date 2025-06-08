package org.baioret.core.worker;

import org.baioret.core.entity.ScheduledTask;
import org.baioret.core.enums.TaskStatus;
import org.baioret.core.holder.ErrorHandlerHolder;
import org.baioret.core.holder.ServiceHolder;
import org.baioret.core.logging.LogService;
import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.service.task.TaskErrorHandler;
import org.baioret.core.service.task.TaskService;

import java.util.UUID;

public class TaskWorker implements Runnable {
    private final String category;
    private final UUID workerId;
    private final TaskService taskService;
    private final TaskErrorHandler taskErrorHandler;

    private boolean doStop = false;

    public TaskWorker(String category, UUID workerId) {
        this.workerId = workerId;
        this.taskService = ServiceHolder.getTaskService();
        this.taskErrorHandler = ErrorHandlerHolder.getTaskErrorHandler();
        this.category = category;
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean keepRunning() {
        return !this.doStop;
    }

    public synchronized boolean executeTask(ScheduledTask task) {
        try {
            Schedulable taskClass = (Schedulable) Class.forName(task.getPath()).getDeclaredConstructor().newInstance();
            return (taskClass.execute(task.getParams()));

        } catch (Exception e) {
            LogService.logger.severe(e.getMessage());
            return false;
        }
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
                    LogService.logger.info(String.format("Worker %s start execute task with id %s and category '%s'",
                            workerId, nextTask.getId(), category));
                    if (executeTask(nextTask)) {
                        taskService.changeTaskStatus(nextTask.getId(), TaskStatus.COMPLETED, category);
                        LogService.logger.info(String.format("Task with id %s and category '%s' has been executed",
                                nextTask.getId(), category));
                    } else {
                        LogService.logger.severe(String.format("Task with id %s and category '%s' failed during execution",
                                nextTask.getId(), category));
                        taskErrorHandler.tryToReschedule(nextTask);
                    }
                }
            }
            LogService.logger.info(String.format("Worker with id %s and category '%s' stopped", workerId, category));
        } catch (Exception e) {
            LogService.logger.severe(e.getMessage());
        }
    }
}