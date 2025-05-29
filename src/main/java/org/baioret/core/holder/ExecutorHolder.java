package org.baioret.core.holder;

import org.baioret.core.service.task.TaskExecutor;

public class ExecutorHolder {
    private static class TaskExecutorHolderInstance {
        static final TaskExecutor INSTANCE = new TaskExecutor(ServiceHolder.getTaskService(), ServiceHolder.getRetryService());
    }

    public static TaskExecutor getTaskExecutor() {
        return TaskExecutorHolderInstance.INSTANCE;
    }
}