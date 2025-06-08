package org.baioret.core.entity;

public class RetryParams {
    private Long taskId;
    private int maxAttempts = 0;
    private boolean delayValueIsFixed = true;
    private Long fixedDelayValue = null;
    private double delayBase = 0;
    private Long delayLimit = null;

    public RetryParams(Long taskId) {
        this.taskId = taskId;
    }

    public RetryParams(Long taskId, int maxAttempts, boolean delayValueIsFixed, Long fixedDelayValue, double delayBase, Long delayLimit) {
        this.taskId = taskId;
        this.maxAttempts = maxAttempts;
        this.delayValueIsFixed = delayValueIsFixed;
        this.fixedDelayValue = fixedDelayValue;
        this.delayBase = delayBase;
        this.delayLimit = delayLimit;
    }


    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean delayValueIsFixed() {
        return delayValueIsFixed;
    }

    public void setDelayValueIsFixed(boolean delayValueIsFixed) {
        this.delayValueIsFixed = delayValueIsFixed;
    }

    public Long getFixedDelayValue() {
        return fixedDelayValue;
    }

    public void setFixedDelayValue(Long fixedDelayValue) {
        this.fixedDelayValue = fixedDelayValue;
    }

    public double getDelayBase() {
        return delayBase;
    }

    public void setDelayBase(double delayBase) {
        this.delayBase = delayBase;
    }

    public Long getDelayLimit() {
        return delayLimit;
    }

    public void setDelayLimit(Long delayLimit) {
        this.delayLimit = delayLimit;
    }
}
