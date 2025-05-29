package org.baioret.core.service.retry;

public class Retry {
    private final boolean withRetry;
    private final boolean fixedRetryPolicy;
    private final Long delayBase;
    private final Long fixDelayValue;
    private final int maxRetryCount;
    private final Long delayLimit;

    private Retry(boolean withRetry,
                  boolean fixedRetryPolicy,
                  Long delayBase,
                  Long fixDelayValue,
                  int maxRetryCount,
                  Long delayLimit) {
        this.withRetry = withRetry;
        this.fixedRetryPolicy = fixedRetryPolicy;
        this.delayBase = delayBase;
        this.fixDelayValue = fixDelayValue;
        this.maxRetryCount = maxRetryCount;
        this.delayLimit = delayLimit;
    }

    public boolean isWithRetry() {
        return withRetry;
    }

    public boolean isFixedRetryPolicy() {
        return fixedRetryPolicy;
    }

    public Long getDelayBase() {
        return delayBase;
    }

    public Long getFixDelayValue() {
        return fixDelayValue;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public Long getDelayLimit() {
        return delayLimit;
    }

    @Override
    public String toString() {
        return "Delay{" +
                "withRetry=" + withRetry +
                ", fixedRetryPolicy=" + fixedRetryPolicy +
                ", delayBase=" + delayBase +
                ", fixDelayValue=" + fixDelayValue +
                ", maxRetryCount=" + maxRetryCount +
                ", delayLimit=" + delayLimit +
                '}';
    }

    public static class RetryBuilder {
        private boolean withRetry = false;
        private boolean fixedRetryPolicy = false;
        private Long delayBase = 0L;
        private Long fixDelayValue = 0L;
        private int maxRetryCount = 0;
        private Long delayLimit = 0L;

        public RetryBuilder setWithRetry(boolean withRetry) {
            this.withRetry = withRetry;
            return this;
        }

        public RetryBuilder setFixedRetryPolicy(boolean fixedRetryPolicy) {
            this.fixedRetryPolicy = fixedRetryPolicy;
            return this;
        }

        public RetryBuilder setDelayBase(Long delayBase) {
            this.delayBase = delayBase;
            return this;
        }

        public RetryBuilder setFixDelayValue(Long fixDelayValue) {
            this.fixDelayValue = fixDelayValue;
            return this;
        }

        public RetryBuilder setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public RetryBuilder setDelayLimit(Long delayLimit) {
            this.delayLimit = delayLimit;
            return this;
        }

        public Retry build() {
            return new Retry(
                    withRetry, fixedRetryPolicy,
                    delayBase, fixDelayValue,
                    maxRetryCount, delayLimit);
        }
    }
}
