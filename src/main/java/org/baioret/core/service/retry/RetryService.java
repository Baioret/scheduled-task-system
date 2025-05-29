package org.baioret.core.service.retry;

import org.baioret.core.entity.RetryParams;

public interface RetryService {
    RetryParams getRetryParams(Long taskId, String category);

    void save(RetryParams retryParams, String category);
}
