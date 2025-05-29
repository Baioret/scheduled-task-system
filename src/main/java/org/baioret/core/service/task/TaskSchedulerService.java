package org.baioret.core.service.task;

import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.service.retry.Retry;

import java.util.Map;
import java.util.Optional;

public interface TaskSchedulerService {
    <T extends Schedulable> boolean cancelTask(Long id, Class<T> schedulable);
    <T extends Schedulable> Optional<Long> scheduleTask(Class<T> schedulable, Map<String, String> params,
                                                        String executionTime, Retry retry);
}
