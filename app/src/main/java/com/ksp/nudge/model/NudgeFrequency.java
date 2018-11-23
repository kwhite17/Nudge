package com.ksp.nudge.model;

public enum NudgeFrequency {
    ONCE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    END_OF_MONTH("Monthly: Last Day");
    private String displayText;

    public String getDisplayText() {
        return displayText;
    }

    NudgeFrequency(String displayText) {
        this.displayText = displayText;
    }

    public static NudgeFrequency fromDisplayText(String displayText) {
        for (NudgeFrequency frequency : NudgeFrequency.values()) {
            if (frequency.getDisplayText().equals(displayText)) {
                return frequency;
            }
        }
        throw new IllegalArgumentException(String.format("No NudgeFrequency for display text: %s", displayText));
    }
}
