package org.baioret.core.service.task;

import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.service.retry.Retry;

import java.util.Map;
import java.util.Optional;

public interface TaskSchedulerService {
    boolean cancelTask(Long id, String category);
    <T extends Schedulable> Optional<Long> scheduleTask(Class<T> scheduleClass, Map<String, String> params,
                                                        String executionTime, Retry retry);
}
