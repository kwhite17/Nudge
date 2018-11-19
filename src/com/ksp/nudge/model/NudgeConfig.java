package com.ksp.nudge.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.joda.time.Instant;

import java.util.List;

/**
 * Created by kevin on 9/24/2018.
 */

@Entity(tableName = "nudges")
public class NudgeConfig {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String message;
    private String frequency;
    private Instant sendTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Instant getSendTime() {
        return sendTime;
    }

    public void setSendTime(Instant sendTime) {
        this.sendTime = sendTime;
    }
}
