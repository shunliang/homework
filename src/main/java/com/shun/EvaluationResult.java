package com.shun;


public class EvaluationResult {
    private boolean isAlertActivated;
    private String description;

    // Constructor
    public EvaluationResult() {
    }

    public EvaluationResult(boolean isAlertActivated, String description) {
        this.isAlertActivated = isAlertActivated;
        this.description = description;
    }

    // Getter and Setter
    public boolean isAlertActivated() {
        return isAlertActivated;
    }

    public void setAlertActivated(boolean alertActivated) {
        isAlertActivated = alertActivated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "EvaluationResult { " +
                "isAlertActivated=" + isAlertActivated +
                ", description='" + description + '\'' +
                " }";
    }
}