package org.baioret.core.repository;

import org.baioret.core.entity.RetryParams;

public interface RetryRepository {
    RetryParams getRetryParams(Long taskId, String category);
    void save(RetryParams delayParams, String category);
}
