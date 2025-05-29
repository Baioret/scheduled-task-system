package org.baioret.core.holder;

import org.baioret.core.service.retry.RetryPolicy;
import org.baioret.core.service.retry.RetryService;
import org.baioret.core.service.task.DatabaseTaskService;
import org.baioret.core.service.task.TaskService;

public class ServiceHolder {
    private static class TaskServiceHolderInstance {
        static final TaskService INSTANCE = new DatabaseTaskService(RepositoryHolder.getTaskInstance());
    }

    public static TaskService getTaskService() {
        return TaskServiceHolderInstance.INSTANCE;
    }

    private static class RetryServiceHolderInstance {
        static final RetryService INSTANCE = new RetryPolicy(RepositoryHolder.getRetryInstance());
    }

    public static RetryService getRetryService() {
        return RetryServiceHolderInstance.INSTANCE;
    }
}
