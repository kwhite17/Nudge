package com.ksp.nudge.model;

import java.util.List;

/**
 * The Nudge class is data structure responsible for maintaining all the information a user
 * provides about the Nudge they are attempting to create.
 */
public class Nudge {
    private NudgeConfig nudgeConfig;
    private List<Recipient> recipients;

    public NudgeConfig getNudgeConfig() {
        return nudgeConfig;
    }

    public void setNudgeConfig(NudgeConfig nudgeConfig) {
        this.nudgeConfig = nudgeConfig;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }
}
