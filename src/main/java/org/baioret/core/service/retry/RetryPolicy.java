package org.baioret.core.service.retry;

import org.baioret.core.entity.RetryParams;
import org.baioret.core.logging.LogService;
import org.baioret.core.repository.RetryRepository;

public class RetryPolicy implements RetryService {

    private final RetryRepository retryRepository;

    public RetryPolicy(RetryRepository retryRepository) {
        this.retryRepository = retryRepository;
    }

    @Override
    public RetryParams getRetryParams(Long taskId, String category) {
        RetryParams retryParams = retryRepository.getRetryParams(taskId, category);
        if (retryParams != null) {
            return retryParams;
        } else {
            throw new RuntimeException(String.format("Can not get retry parameters: task with id %s and category '%s' not found",
                    taskId, category));
        }
    }

    @Override
    public void save(RetryParams retryParams, String category) {
        retryRepository.save(retryParams, category);
        LogService.logger.info(String.format("Retry params for task with id %s and category '%s' successfully created: object %s",
                retryParams.getTaskId(), category, retryParams));
    }
}