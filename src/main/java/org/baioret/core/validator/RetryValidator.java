package org.baioret.core.validator;

import org.baioret.core.logging.LogService;
import org.baioret.core.service.retry.Retry;

public class RetryValidator {
    public static boolean validateParams(Retry params) {

        if (params == null) {
            LogService.logger.severe("Retry params is null");
            return false;
        }

        return validateParams(params.isWithRetry(), params.isFixedRetryPolicy(), params.getDelayBase(), params.getFixDelayValue(), params.getMaxRetryCount(), params.getDelayLimit());
    }

    private static boolean validateParams(boolean withRetry, boolean fixedRetryPolicy, Long delayBase, Long fixDelayValue, int maxRetryCount, Long delayLimit) {

        if (withRetry) {
            if (fixedRetryPolicy) {
                if (fixDelayValue <= 0) {
                    LogService.logger.severe("You are trying to schedule task with retry and fixed delay policy. " +
                            "Fix delay value should be greater than 0. You have set the value: " + fixDelayValue);
                    return false;
                }
            } else {
                if (delayBase < 0) {
                    LogService.logger.severe("You are trying to schedule task with retry and function delay policy. " +
                            "Delay base should be 0 (default) or greater than 0. You have set the value: " + delayBase);
                    return false;
                }

                if (delayLimit <= 0) {
                    LogService.logger.severe("You are trying to schedule task with retry. " +
                            "Delay limit should be greater than 0. You have set the value: " + delayLimit);
                    return false;
                }
            }
            if (maxRetryCount <= 0) {
                LogService.logger.severe("You are trying to schedule task with retry. " +
                        "Max retry count should be greater than 0. You have set the value: " + maxRetryCount);
                return false;
            }
        }
        return true;
    }
}
