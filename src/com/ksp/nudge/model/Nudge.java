package com.ksp.nudge.model;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * The Nudge class is data structure responsible for maintaining all the information a user
 * provides about the Nudge they are attempting to create.
 */
public class Nudge {
    private NudgeConfig nudgeConfig;
    private List<Recipient> recipients;
    private String id;

    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
    }

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

    public static Nudge getDefaultInstance() {
        Nudge nudge = new Nudge();
        NudgeConfig config = new NudgeConfig();
        nudge.setRecipients(new ArrayList<Recipient>());
        nudge.setNudgeConfig(config);
        return nudge;
    }
}
