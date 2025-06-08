package org.baioret.core.entity;

import org.baioret.core.enums.TaskStatus;

import java.sql.Timestamp;
import java.util.Map;


public class ScheduledTask {

    private Long id;
    private String category;
    private String path;
    private Map<String, String> params;
    private TaskStatus status = TaskStatus.PENDING;
    private Timestamp executionTime;
    private int attempt = 1;

    public ScheduledTask(String category, Timestamp executionTime) {
        this.category = category;
        this.executionTime = executionTime;
    }

    public ScheduledTask() {
        this.category = "DoSomething";
        this.path = "org.baioret.test.DoSomething";
        this.params = Map.of("ID", "123",
                "message", "Test message");
        this.executionTime = new Timestamp(System.currentTimeMillis());
    }


    public String toString() {
        return
                "Task " + this.id +
                        ": category: " + this.category +
                        ", status: " + this.status +
                        ", execution time: " + this.executionTime +
                        ", attempt: " + this.attempt;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Timestamp getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Timestamp executionTime) {
        this.executionTime = executionTime;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
