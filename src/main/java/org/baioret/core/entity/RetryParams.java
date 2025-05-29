package org.baioret.core.entity;

public class RetryParams {
    private Long taskId;
    private boolean withRetry = false;
    private int retryCount = 0;
    private boolean valueIsFixed = true;
    private Long fixDelayValue = null;
    private Long delayBase = null;
    private Long delayLimit = null;

    public RetryParams(Long taskId) {
        this.taskId = taskId;
    }

    public RetryParams(Long taskId, boolean withRetry, int retryCount, boolean valueIsFixed, Long fixDelayValue, Long delayBase, Long delayLimit) {
        this.taskId = taskId;
        this.withRetry = withRetry;
        this.retryCount = retryCount;
        this.valueIsFixed = valueIsFixed;
        this.fixDelayValue = fixDelayValue;
        this.delayBase = delayBase;
        this.delayLimit = delayLimit;
    }


    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public boolean isWithRetry() {
        return withRetry;
    }

    public void setWithRetry(boolean withRetry) {
        this.withRetry = withRetry;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isValueIsFixed() {
        return valueIsFixed;
    }

    public void setValueIsFixed(boolean valueIsFixed) {
        this.valueIsFixed = valueIsFixed;
    }

    public Long getFixDelayValue() {
        return fixDelayValue;
    }

    public void setFixDelayValue(Long fixDelayValue) {
        this.fixDelayValue = fixDelayValue;
    }

    public Long getDelayBase() {
        return delayBase;
    }

    public void setDelayBase(Long delayBase) {
        this.delayBase = delayBase;
    }

    public Long getDelayLimit() {
        return delayLimit;
    }

    public void setDelayLimit(Long delayLimit) {
        this.delayLimit = delayLimit;
    }
}
