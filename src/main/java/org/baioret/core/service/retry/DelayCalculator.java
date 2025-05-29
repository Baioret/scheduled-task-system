package org.baioret.core.service.retry;

public class DelayCalculator {
    static private final double defaultBase = Math.E;

    static public long getNextDelay(int attemptCount) {
        return (long)Math.pow(defaultBase, attemptCount);
    }

    static public long getNextDelay(int attemptCount, double base) {
        return (long)Math.pow(base, attemptCount);
    }

    static public long getNextDelay(int attemptCount, long limit) {
        return getNextDelay(attemptCount, defaultBase, limit);
    }

    static public long getNextDelay(int attemptCount, double base, long limit) {
        long delay = (long)Math.pow(base, attemptCount);
        return delay <= limit ? delay : -1;
    }
}
