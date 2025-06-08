package org.baioret.core.service.retry;

public class Retry {
    private final boolean fixedRetryPolicy;
    private final double delayBase;
    private final Long fixedDelayValue;
    private final int maxRetryCount;
    private final Long delayLimit;

    private Retry(
                  boolean fixedRetryPolicy,
                  double delayBase,
                  Long fixedDelayValue,
                  int maxRetryCount,
                  Long delayLimit) {
        this.fixedRetryPolicy = fixedRetryPolicy;
        this.delayBase = delayBase;
        this.fixedDelayValue = fixedDelayValue;
        this.maxRetryCount = maxRetryCount;
        this.delayLimit = delayLimit;
    }

    public boolean isFixedRetryPolicy() {
        return fixedRetryPolicy;
    }

    public double getDelayBase() {
        return delayBase;
    }

    public Long getFixDelayValue() {
        return fixedDelayValue;
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
                "fixedRetryPolicy=" + fixedRetryPolicy +
                ", delayBase=" + delayBase +
                ", fixedDelayValue=" + fixedDelayValue +
                ", maxAttemptsCount=" + maxRetryCount +
                ", delayLimit=" + delayLimit +
                '}';
    }

    public static class RetryBuilder {
        private boolean fixedRetryPolicy = false;
        private double delayBase = Math.E;
        private Long fixedDelayValue = 0L;
        private int maxAttemptsCount = 0;
        private Long delayLimit = 0L;


        public RetryBuilder setFixedRetryPolicy(boolean fixedRetryPolicy) {
            this.fixedRetryPolicy = fixedRetryPolicy;
            return this;
        }

        public RetryBuilder setDelayBase(Long delayBase) {
            this.delayBase = delayBase;
            return this;
        }

        public RetryBuilder setFixDelayValue(Long fixedDelayValue) {
            this.fixedDelayValue = fixedDelayValue;
            return this;
        }

        public RetryBuilder setMaxAttemptsCount(int maxAttemptsCount) {
            this.maxAttemptsCount = maxAttemptsCount;
            return this;
        }

        public RetryBuilder setDelayLimit(Long delayLimit) {
            this.delayLimit = delayLimit;
            return this;
        }

        public Retry build() {
            return new Retry(
                    fixedRetryPolicy,
                    delayBase, fixedDelayValue,
                    maxAttemptsCount, delayLimit);
        }
    }
}
