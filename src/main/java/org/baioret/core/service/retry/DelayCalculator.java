package org.baioret.core.service.retry;

public class DelayCalculator {
    static private final double defaultBase = Math.E;

    static public long getNextDelay(int attemptCount, double base, long limit) {
        long delay;
        if (base > 0) delay = (long)Math.pow(base, attemptCount);
        else delay = (long)Math.pow(defaultBase, attemptCount);
        return delay <= limit ? delay : -1;
    }
}