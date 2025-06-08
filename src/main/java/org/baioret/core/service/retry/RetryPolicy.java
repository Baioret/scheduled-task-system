package org.baioret.core.service.retry;

import org.baioret.core.entity.RetryParams;
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
            return null;
        }
    }

    @Override
    public void save(RetryParams retryParams, String category) {
        retryRepository.save(retryParams, category);
    }
}