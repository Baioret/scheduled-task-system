package org.baioret.core.holder;

import org.baioret.core.service.task.TaskErrorHandler;

public class ErrorHandlerHolder {
    private static class TaskErrorHandlerHolderInstance {
        static final TaskErrorHandler INSTANCE = new TaskErrorHandler(ServiceHolder.getTaskManager());
    }

    public static TaskErrorHandler getTaskErrorHandler() {
        return TaskErrorHandlerHolderInstance.INSTANCE;
    }
}